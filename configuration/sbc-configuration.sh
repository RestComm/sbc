#!/bin/sh

USER=administrator@company.com
PASS=RestComm

chkConnection () {
HTTP_STATUS=$(curl -u $USER:$PASS -k -X GET -w "%{http_code}" -so /dev/null 'https://127.0.0.1:8443/restcomm-sbc/2012-04-24/NetworkPoints')
echo "$HTTP_STATUS"
}
login () {
read -e -i "$USER" -p "Login: " input
USER="${input:-$USER}"
read -e -s -i "$PASS" -p "Password: " input
PASS="${input:-$PASS}"
echo
echo -n "login with $USER STAT="
chkConnection
}
read_dom () {
    local IFS=\>
    read -r -d \< ENTITY CONTENT
}
gmenu () {
echo
echo "SBC Configuration"
echo
echo 1 Network Points
echo 2 Connectors
echo 3 Routes
echo q Quit
echo
echo -n Option: ; read OPTION
}
nmenu () {
while [ 1 ]; do
echo
echo "Network Points"
echo
echo 1 Tag
echo 2 UnTag
echo 3 List
echo q Quit
echo
echo -n Option: ; read OPTION

        case $OPTION in
                1)
                echo "Tag Network Points"
                echo
		ip route show
		echo
                showNpoints
                echo -n "#:            "; read ORDER
                echo -n "Tag: [MZ|DMZ] "; read TAG
                eval "NPO=\$NP$ORDER"
                echo "tag $NPO $TAG"
                tag $NPO $TAG
                ;;
                2)
                echo "UnTag Network Points"
                echo
		ip route show
		echo
                showNpoints
                echo -n "#:           "; read ORDER

                eval "NPO=\$NP$ORDER"
                echo "untag $NPO"
                untag $NPO 
                ;;
                3)
                echo
                showNpoints
                ;;
                q)
                break
                ;;
        esac
done
}
cmenu () {
while [ 1 ]; do
echo
echo "Connectors"
echo
echo 1 Add
echo 2 Remove
echo 3 List
echo 4 Plug
echo 5 Unplug
echo q Quit
echo
echo -n Option: ; read OPTION

case $OPTION in
        1)
        echo "Add Connector"
        echo
        echo -n "Iface:        "; read ID
        echo    "Transport:    "
        echo -n "[UDP|TCP|WSS] "; read TRANSPORT
        echo -n "Port:         "; read PORT

        addConnector $ID $TRANSPORT $PORT
        ;;
        2)
        echo "Remove Connector"
        echo
        showConnectors
        echo -n "#: "; read ORDER

        eval "CPO=\$CO$ORDER"
        echo "remove $CPO"
        delConnector $CPO
        ;;

        3)
        echo
        showConnectors
        ;;
        4)
        echo "Plug Connector"
        echo
        showConnectors
        echo -n "#: "; read ORDER

        eval "CPO=\$CO$ORDER"
        echo "plug $CPO"
        plugConnector $CPO
        ;;
        5)
        echo "UnPlug Connector"
        echo
        showConnectors
        echo -n "#: "; read ORDER

        eval "CPO=\$CO$ORDER"
        echo "unplug $CPO"
        uplugConnector $CPO
        ;;
        q)
        break
        ;;
esac
done
}
rmenu () {
while [ 1 ]; do
echo
echo "Routes"
echo
echo 1 Add
echo 2 Remove
echo 3 List
echo q Quit
echo
echo -n Option: ; read OPTION
case $OPTION in
        1)
        echo "Add Route"
        echo
        showConnectors
        showRoutes
        echo -n "Source #: "; read SORDER
        echo -n "Target #: "; read TORDER

        eval "SRPO=\$CO$SORDER"
        eval "TRPO=\$CO$TORDER"
        echo "add $SRPO $TRPO"
        addRoute $SRPO $TRPO
        ;;
        2)
        echo "Remove Route"
        echo
        showRoutes
        echo -n "#: "; read ORDER

        eval "RPO=\$RO$ORDER"
        echo "remove $RPO "
        delRoute $RPO
        ;;
        3)
        echo
        showRoutes
        ;;
        q)
        break
        ;;
esac
done
}


function showNpoints() {
curl -u $USER:$PASS -k -X GET  https://127.0.0.1:8443/restcomm-sbc/2012-04-24/NetworkPoints > /tmp/npoints.xml 2>/dev/null
echo -e "#\tId\tMAC Address\tTag"
NPOINT=0
while read_dom; do
    if [[ $ENTITY = "Id" ]] ; then
        ((NPOINT++))
        export NP${NPOINT}=$CONTENT
        echo -n -e "$NPOINT\t"
        echo -n -e $CONTENT
    fi
    if [[ $ENTITY = "MacAddress" ]] ; then
        echo -n -e "\t$CONTENT"
    fi
    if [[ $ENTITY = "Tag" ]] ; then
        echo -e "\t$CONTENT"
    fi
done < /tmp/npoints.xml
}

function showRoutes() {
curl -u $USER:$PASS -k -X GET  https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Routes > /tmp/routes.xml 2>/dev/null
echo -e "#\tSource(DMZ)\t\t\tTarget(MZ)"
RPOINT=0
while read_dom; do
    if [[ $ENTITY = "Sid" ]] ; then
        ((RPOINT++))
        export RO${RPOINT}=$CONTENT
        echo -n -e "$RPOINT\t"
#       echo -n -e "$CONTENT\t"
    fi
    if [[ $ENTITY = "SourceConnectorSid" ]] ; then
        showConnector $CONTENT
    fi
    if [[ $ENTITY = "TargetConnectorSid" ]] ; then
        echo -n -e "\t"
        showConnector $CONTENT
        echo
    fi
done < /tmp/routes.xml
}
function showConnector() {
curl -u $USER:$PASS -k -X GET  https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors/$1 > /tmp/cnct.xml 2>/dev/null
while read_dom; do
    if [[ $ENTITY = "IpAddress" ]] ; then
        echo -n -e "$CONTENT"
    fi
#   if [[ $ENTITY = "NetworkPointId" ]] ; then
#       echo -n /
#        echo -n -e "$CONTENT"
#    fi
    if [[ $ENTITY = "Transport" ]] ; then
        echo -n /
        echo -n -e "$CONTENT"
    fi
    if [[ $ENTITY = "Port" ]] ; then
         echo -n :
        echo -n -e "$CONTENT"
    fi
    if [[ $ENTITY = "State" ]] ; then
        echo -n /
        echo -n -e "$CONTENT"
    fi
done < /tmp/cnct.xml
}
function showConnectors() {
curl -u $USER:$PASS -k -X GET  https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors > /tmp/cncts.xml 2>/dev/null
echo -e "#\tIP Address\tPort\tIface\tTrans\tStatus"
CPOINT=0
while read_dom; do
    if [[ $ENTITY = "IpAddress" ]] ; then
        ((CPOINT++))
        echo -n -e "$CPOINT\t"
        echo -n -e "$CONTENT"
    fi
    if [[ $ENTITY = "Sid" ]] ; then
        export CO${CPOINT}=$CONTENT
#       echo -n -e "\t$CONTENT"
    fi
    if [[ $ENTITY = "NetworkPointId" ]] ; then
        echo -n -e "\t$CONTENT"
    fi
    if [[ $ENTITY = "Transport" ]] ; then
        echo -n -e "\t$CONTENT"
    fi
    if [[ $ENTITY = "Port" ]] ; then
        echo -n -e "\t$CONTENT"
    fi
    if [[ $ENTITY = "State" ]] ; then
        echo -e "\t$CONTENT"
    fi
done < /tmp/cncts.xml
}
function tag() {
curl -u $USER:$PASS -k -X POST https://127.0.0.1:8443/restcomm-sbc/2012-04-24/NetworkPoints/ -d "Id=$1" -d "Tag=$2"  > /dev/null 2>/dev/null
}
function untag() {
curl -u $USER:$PASS -k -X DELETE https://127.0.0.1:8443/restcomm-sbc/2012-04-24/NetworkPoints/$1 > /dev/null 2>/dev/null
}
function addConnector() {
curl -u $USER:$PASS -k -X POST https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors/ -d "NetworkPointId=$1" -d "Transport=$2" -d "Port=$3"  > /dev/null 2>/dev/null
}
function delConnector() {
curl -u $USER:$PASS -k -X DELETE https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors/$1  > /dev/null 2>/dev/null
}
function plugConnector() {
curl -u $USER:$PASS -k -X POST https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors/$1 -d "State=UP"  > /dev/null 2>/dev/null
}
function uplugConnector() {
 curl -u $USER:$PASS -k -X POST https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Connectors/$1 -d "State=DOWN"  > /dev/null 2>/dev/null
}
function addRoute() {
curl -u $USER:$PASS -k -X POST https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Routes/ -d "SourceConnectorSid=$1" -d "TargetConnectorSid=$2"  > /dev/null 2>/dev/null
}
function delRoute() {
curl -u $USER:$PASS -k -X DELETE https://127.0.0.1:8443/restcomm-sbc/2012-04-24/Routes/$1  > /dev/null 2>/dev/null
}

login
while [ 1 ]; do
gmenu
case $OPTION in
        1)
        nmenu
        ;;
        2)
        cmenu
        ;;
        3)
        rmenu
        ;;
        q)
        exit 0;
        ;;
        *)
        echo "Unknown option!"
        ;;
esac
done
                
