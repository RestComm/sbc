#!/bin/sh
CERTDIR="../conf/certs"
KEYSTORE="$CERTDIR/keystore"
KEYSTORE_WEB="$KEYSTORE-web.jks"
KEYSTORE_DTLS="$KEYSTORE-dtls.jks"



build() {
echo '<keytool> Creating SBC Web Server key ...'
keytool -genkey -noprompt \
 -keyalg RSA \
 -alias sbc-server-web \
 -keystore $KEYSTORE_WEB \
 -keypass changeME \
 -storepass changeME \
 -validity 365 \
 -keysize 2048 \
 -dname "CN=SBC Project, OU=Osky, O=sbc.eolos.la, C=US"

echo '<keytool> Creating SBC DTLS Server key ...'
keytool -genkey -noprompt \
 -keyalg EC \
 -alias sbc-server-dtls \
 -keystore $KEYSTORE_DTLS \
 -storepass changeME \
 -keypass changeME \
 -validity 365 \
 -keysize 256 \
 -sigalg SHA256withECDSA \
 -dname "CN=SBC Project, OU=Osky, O=sbc.eolos.la, C=US"

echo '<keytool> Exporting SBC DTLS Server Certificate ...'
keytool -exportcert -noprompt \
  -alias sbc-server-dtls  \
 -keystore $KEYSTORE_DTLS \
 -storepass changeME \
 -rfc \
 -file $CERTDIR/x509-server-ecdsa.pem
echo '<keytool> Importing SBC DTLS Keystore in portable format ...'
keytool -importkeystore -noprompt \
 -srcalias sbc-server-dtls \
 -srckeystore $KEYSTORE_DTLS \
 -destkeystore $CERTDIR/server.p12 \
 -srckeypass changeME \
 -srcstorepass changeME \
 -srcstoretype jks \
 -destkeypass changeME \
 -deststorepass changeME \
 -deststoretype pkcs12
echo '<openssl> Exporting SBC DTLS Private key ...'
openssl pkcs12 \
 -in $CERTDIR/server.p12 \
 -nocerts \
 -nodes \
 -password pass:changeME \
 -passin pass:changeME \
 -passout pass:changeME \
 -out $CERTDIR/x509-server-key-ecdsa.pem

}

rebuild() {
rm -rf $CERTDIR
mkdir $CERTDIR
build
}

patchStack() {
DIR=$PWD/..
sed "s#__PWD__#$DIR#g" ../conf/mss-sip-stack.properties.template > ../conf/mss-sip-stack.properties
}

buildcerts() {
patchStack
if [ ! -d $CERTDIR ]; then
        mkdir $CERTDIR
        build
else
	rebuild
fi
}
