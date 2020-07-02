package be.ugent.blok2.model.users;


/**
 * Note: if you change anything here, you also must change the value of the 'roles' variable in the
 * frontend's environment/environment.ts file and in the IRoles interface. The reason is that spring the names of
 * a enum sends when creating JSON objects and the frontend must recognize the user.
 *
 * E.g. User {name = "John Doe", roles = new Role[]{Role.STUDENT}} will become {"name": "John Doe", "roles" = ["STUDENT"]}
 * and the frontend must recognize STUDENT
*/
public enum Role {
    STUDENT,
    EMPLOYEE,
    ADMIN;
}
