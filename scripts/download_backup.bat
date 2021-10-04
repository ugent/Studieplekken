@echo off
FOR /F "tokens=* USEBACKQ" %%G IN (`ssh -p 2222 root@fireball.ugent.be "cd /mnt/dsashare/fireball/backup/database && ls | egrep -v 'deploy' | tail -n 1"`) DO (
set dsa_blokat_backup_loc=/mnt/dsashare/fireball/backup/database/%%G
)
echo Found backup %dsa_blokat_backup_loc%
echo Attempting to download.

scp -P 2222 root@fireball.ugent.be:%dsa_blokat_backup_loc% .