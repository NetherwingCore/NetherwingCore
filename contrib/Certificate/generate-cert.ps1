# Criar configuração SSL
@"
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
C = US
ST = California
L = Irvine
O = Blizzard Entertainment
CN = *.actual.battle.net

[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = *.actual.battle.net
DNS.2 = us.actual.battle.net
DNS.3 = eu.actual.battle.net
DNS.4 = kr.actual.battle.net
DNS.5 = cn.actual.battle.net
"@ | Out-File -Encoding ASCII openssl.cnf

# Generate private key
openssl genrsa -out netherwingcore_bnetserver.key 2048

# Generate certificate
openssl req -new -x509 -key netherwingcore_bnetserver.key -out netherwingcore_bnetserver.crt -days 10950 -config openssl.cnf -extensions v3_req

# Convert to PKCS12
openssl pkcs12 -export -in netherwingcore_bnetserver.crt -inkey netherwingcore_bnetserver.key -out netherwingcore_keystore.p12 -name netherwingcore_bnetserver -password pass:netherwing

# Convert to JKS
keytool -importkeystore -srckeystore netherwingcore_keystore.p12 -srcstoretype PKCS12 -destkeystore netherwingcore_keystore.jks -deststoretype JKS -srcstorepass netherwing -deststorepass netherwing -noprompt

Write-Host "✅ Certificates generated successfully.!" -ForegroundColor Green
Write-Host ""
Write-Host "Files created:"
Write-Host "  - bnetserver.key (private key)"
Write-Host "  - bnetserver.crt (certificate)"
Write-Host "  - keystore.p12 (PKCS12)"
Write-Host "  - keystore.jks (Java KeyStore)"