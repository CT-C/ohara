#!/bin/bash
#
# Copyright 2019 is-land
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

usage="USAGE: $0 [start|stop|--help] --user"' ${USER_NAME} --password ${PASSWORD} ....'

if [ $# -lt 1 ];
then
  echo $usage
  exit 1
fi

COMMAND=$1
case $COMMAND in
  start)
    start="true"
    shift
    ;;
  stop)
    stop="true"
    shift
    ;;
  --help)
    help="true"
    shift
    ;;
  *)
    echo $usage
    exit 1
    ;;
esac

if [ "${help}" == "true" ];
then
  echo $usage
  echo "Argument             Description"
  echo "--------             -----------"
  echo "--user               Set oracle database user name"
  echo "--password           Set oracle database password"
  echo "--port               Set connection port for client"
  echo "--sid                Set connection sid. example: --sid xe"
  echo "--host               Set host name to remote host the oracle database container"
  exit 1
fi

sid="xe"
port="1521"
containerName="oracle-benchmark-test"

ARGUMENT_LIST=("user" "password" "port" "sid" "host")

opts=$(getopt \
    --longoptions "$(printf "%s:," "${ARGUMENT_LIST[@]}")" \
    --name "$(basename "$0")" \
    --options "" \
    -- "$@"
)
eval set --$opts

while [[ $# -gt 0 ]]; do
  case "$1" in
    --user)
      user=$2
      shift 2
      ;;
    --password)
      password=$2
      shift 2
      ;;
    --port)
      port=$2
      shift 2
      ;;
    --sid)
      sid=$2
      shift 2
      ;;
    --host)
      host=$2
      shift 2
      ;;
    *)
      break
      ;;
  esac
done

if [[ -z ${user} ]] && [[ "${start}" == "true" ]];
then
  echo 'Please set the --user ${USER_NAME} argument'
  exit 1
fi

if [[ -z ${password} ]] && [[ "${start}" == "true" ]];
then
  echo 'Please set the --password ${PASSWORD} argument'
  exit 1
fi

if [[ -z ${host} ]];
then
  echo 'Please set the --host ${DEPLOY_ORACLE_DATABASE_CONTAINER_HOSTNAME} argument to deploy oracle database container. example: --host host1'
  exit 1
fi

if [[ "${start}" == "true" ]];
then
echo "Starting oracle database container"
echo "Port is ${port}"
ssh ohara@${host} docker run -d -i --name ${containerName} --restart=always -p ${port}:1521 --env DB_SID=${sid} store/oracle/database-enterprise:12.2.0.1
sleep 3m
ssh ohara@${host} << EOF
docker exec -i $containerName bash -c "source /home/oracle/.bashrc;echo -e 'alter session set \"_ORACLE_SCRIPT\"=true;\ncreate user ${user} identified by ${password};\nGRANT CONNECT, RESOURCE, DBA TO ${user};'|sqlplus sys/Oradoc_db1@${sid} as sysdba"
EOF
echo "Start oracle database complete. User name is ${user}"
fi

if [[ "${stop}" == "true" ]];
then
  echo "Stoping oracle database container"
  ssh ohara@${host} docker rm -f $containerName
fi
