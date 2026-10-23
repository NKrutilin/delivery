#!/bin/bash
set -e
cd "$(dirname "$0")"

if [ -z "$JAVA_HOME" ]; then
  export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/temurin-21.0.6/Contents/Home"
fi

cat > settings-mirror.xml <<'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
    <mirrors>
        <mirror>
            <id>huawei-maven</id>
            <mirrorOf>central</mirrorOf>
            <url>https://repo.huaweicloud.com/repository/maven</url>
        </mirror>
    </mirrors>
</settings>
EOF

echo "==> Pre-downloading all dependencies and plugins via Huawei mirror..."
mvn -s settings-mirror.xml -q dependency:go-offline

echo "==> Building fat jar to verify everything is in place..."
mvn -s settings-mirror.xml -q package -DskipTests

echo "==> Done:"
ls -la target/*.jar
