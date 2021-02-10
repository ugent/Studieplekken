"""
This script migrates the id's of the users from being their student number or email
address if the user is an employee, to being their ugentID. This is way more robust:
e.g. an email address may change

Note that the PSQL-USR, PSQL-PWD, LDAP-USR and LDAP-PWD must be provided as a command line
argument for safety reasons (developers may overlook this file searching for passwords 
before sharing files or making the project public or partially public in any way...)

USAGE: python[3] migrate_users_pks.py <PSQL-HOST> <PSQL-DB-NAME> <PSQL-USR> <PSQL-PWD> <LDAP-USR> <LDAP-PWD>

Useful links:
    - https://medium.com/@alpolishchuk/a-little-python-ldap-tutorial-4a6a79676157
    - https://www.python-ldap.org/en/python-ldap-3.3.0/installing.html
    - https://www.postgresqltutorial.com/postgresql-python/

Dependencies (intall with pip):
    - python-ldap
    - psycopg2
"""
import sys
import re
import ldap
import psycopg2 as psql
import psycopg2.extras

ldap.set_option(ldap.OPT_X_TLS_REQUIRE_CERT, ldap.OPT_X_TLS_NEVER)

# variables that will be set before calling the main, based on sys.argv
DB_HST = ''
DB_NME = ''
DB_USR = ''
DB_PWD = ''
LDAP_USR = ''
LDAP_PWD = ''


def psql_connection():
    """
    Create and return a PostgreSQL connection
    """
    return psql.connect(
        host=DB_HST,
        database=DB_NME,
        user=DB_USR,
        password=DB_PWD
    )


def find_all_augent_ids_to_migrate():
    """
    Retrieve and return all augent_ids from the database
    """
    conn = None
    augent_ids = []
    try:
        conn = psql_connection()
        cur = conn.cursor()
        cur.execute('select augentid from users;')
        print("INFO: the number of augent_ids is {}".format(cur.rowcount))

        row = cur.fetchone()
        while row is not None:
            augent_ids.append(row[0])
            row = cur.fetchone()

        cur.close()
    except psql.DatabaseError as error:
        print(error)
        sys.exit(1)
    finally:
        if conn is not None:
            conn.close()

    return augent_ids


def check_validity_of_retrieved_ugent_ids(ugent_ids):
    """
    Check that all values in the provided list of ugent_ids are all the same.
    Return value is a tuple: (valid: bool, retval: int)
        valid: is the list of ugent_ids valid or not
        retval:
            - valid == True: string -> the valid ugent_id
            - valid == False: error code 1 -> not all ugent_ids are equal
            - valid == False: error code 2 -> the list of ugent_ids is empty
    """
    if len(ugent_ids) > 0:
        check = ugent_ids[0]
        for other in ugent_ids[1:]:
            if check != other:
                return False, 1
        return True, check
    return False, 2


def migrate_ids(migration_ids):
    """
    Migrate all user ids in the database
    """
    conn = None
    try:
        conn = psql_connection()
        cur = conn.cursor()
        update_query = """update users
                           set augentid = data.ugentid
                           from (values %s) as data (augentid, ugentid)
                           where users.augentid = data.augentid;"""

        psql.extras.execute_values(cur, update_query, migration_ids)

        print('INFO: updated {} users in database (note: this is the rowcount of the last batch update of max 100 rows each)'
              .format(cur.rowcount))

        conn.commit()

        cur.close()
    except psql.DatabaseError as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()


def main():
    """
    Main method of script
    """
    connect = ldap.initialize('ldaps://ldap.ugent.be')
    connect.simple_bind_s(LDAP_USR, LDAP_PWD)

    # all augent_ids to migrate
    augent_ids = find_all_augent_ids_to_migrate()

    # auxiliary variables
    migration_ids = []
    tot = len(augent_ids)
    curr = 0
    reported_progress = {}

    # for each augent_id, determine the ugent_id to migrate to
    for augent_id in augent_ids:
        # Search user with augent_id in UGent LDAP
        # Note: the same user may be returned multiple times but with a different base
        result = connect.search_s(
            '***REMOVED***',
            ldap.SCOPE_SUBTREE,
            '(|(ugentStudentID={0})(mail={0}))'.format(augent_id)
        )

        # search for the ugentID[s] in the base[s], which is the first string in the
        # tuple (i.e. res[0])
        ugent_ids = []
        for res in result:
            match = re.search(r'ugentID=(\d+),', res[0])
            ugent_ids.append(match.group(1))

        # check if all retrieved ugent_ids are equally the same
        valid, retval = check_validity_of_retrieved_ugent_ids(ugent_ids)
        if valid:
            migration_ids.append((augent_id, retval))
        elif retval == 1:
            print('WARN: found inconsistend ugentIDs for user with augent_id \'{0}\''
                  .format(augent_id))
        else:
            print('WARN: ugent_ids was empty for augent_id \'{0}\''.format(augent_id))

        # some intermediate report (every 5% progress)
        progress = 100 * round(curr / tot, 2)
        if progress % 5 == 0 and progress not in reported_progress:
            print('PROGRESS: Finished {}% of the ldap searches for the new ugentIDs'
                  .format(progress))
            reported_progress[progress] = True
        curr += 1

    print('INFO: {0}/{1} users can be migrated because ugentIDs were found for the corresponding augentIDs'
          .format(len(augent_ids), len(migration_ids)))

    # finally, migrate all the ids
    migrate_ids(migration_ids)

    sys.exit(0)


if __name__ == '__main__':
    if len(sys.argv) != 7:
        print('USAGE: python[3] migrate_users_pks.py <PSQL-HOST> <PSQL-DB-NAME> <PSQL-USR> <PSQL-PWD> <LDAP-USR> <LDAP-PWD>')
        sys.exit(1)
    else:
        DB_HST = sys.argv[1]
        DB_NME = sys.argv[2]
        DB_USR = sys.argv[3]
        DB_PWD = sys.argv[4]
        LDAP_USR = sys.argv[5]
        LDAP_PWD = sys.argv[6]
    main()
