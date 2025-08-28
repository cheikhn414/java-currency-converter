# 🚀 Installation Guide - Currency Converter

This guide walks you through step-by-step to install and deploy the currency converter application.

## ⚙️ System Prerequisites

### Required Software
- **Java Development Kit (JDK) 21 or higher**
- **Apache Maven 3.6 or higher**
- **Java application server** (recommended: Apache Tomcat 9+)

### Prerequisites Verification
```bash
# Check Java
java -version
# Expected output: java version "21.x.x" or higher

# Check Maven
mvn -version
# Expected output: Apache Maven 3.6.x or higher
```

## 📦 Prerequisites Installation

### On Ubuntu/Debian
```bash
# System update
sudo apt update

# Install Java 21
sudo apt install openjdk-21-jdk

# Install Maven
sudo apt install maven

# Install Tomcat
sudo apt install tomcat9 tomcat9-admin
```

### On CentOS/RHEL/Fedora
```bash
# Install Java 21
sudo dnf install java-21-openjdk-devel

# Install Maven
sudo dnf install maven

# Install Tomcat
sudo dnf install tomcat tomcat-webapps tomcat-admin-webapps
```

### On macOS (with Homebrew)
```bash
# Install Java 21
brew install openjdk@21

# Install Maven
brew install maven

# Install Tomcat
brew install tomcat
```

### On Windows
1. **Java**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
2. **Maven**: Download from [Apache Maven](https://maven.apache.org/download.cgi)
3. **Tomcat**: Download from [Apache Tomcat](https://tomcat.apache.org/download-90.cgi)

## 🏗️ Project Build

### 1. Project preparation
```bash
# Navigate to project directory
cd currency-converter

# Clean and compile
mvn clean compile
```

### 2. Run tests (optional)
```bash
# Run unit tests
mvn test
```

### 3. Application packaging
```bash
# Create WAR file
mvn package

# The WAR file will be created at target/currency-converter.war
ls -la target/currency-converter.war
```

## 🚢 Deployment

### Option 1: Quick Deployment with Maven (Development)

```bash
# Start with embedded Tomcat
mvn tomcat7:run

# The application will be accessible at:
# http://localhost:8080/currency-converter
```

### Option 2: Standalone Tomcat Deployment

#### A. Tomcat Configuration
```bash
# Set environment variables
export CATALINA_HOME=/opt/tomcat  # Adjust according to your installation
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk  # Adjust according to your installation
```

#### B. WAR Deployment
```bash
# Copy the WAR file
sudo cp target/currency-converter.war $CATALINA_HOME/webapps/

# Or for system installation (Ubuntu/Debian)
sudo cp target/currency-converter.war /var/lib/tomcat9/webapps/
```

#### C. Tomcat Users Configuration (optional)
```bash
# Edit tomcat-users.xml file
sudo nano $CATALINA_HOME/conf/tomcat-users.xml

# Add admin user:
```
```xml
<tomcat-users>
  <role rolename="manager-gui"/>
  <role rolename="admin-gui"/>
  <user username="admin" password="admin123" roles="manager-gui,admin-gui"/>
</tomcat-users>
```

#### D. Start Tomcat
```bash
# Start Tomcat
sudo $CATALINA_HOME/bin/startup.sh

# Or for system installation
sudo systemctl start tomcat9
sudo systemctl enable tomcat9  # Auto-start
```

### Option 3: Docker Deployment (Advanced)

#### Create a Dockerfile
```dockerfile
FROM tomcat:9-jdk21-openjdk
COPY target/currency-converter.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

#### Build and run
```bash
# Build Docker image
docker build -t currency-converter .

# Run container
docker run -p 8080:8080 currency-converter
```

## 🔧 Post-Deployment Configuration

### 1. Deployment verification
```bash
# Check that Tomcat is running
sudo systemctl status tomcat9

# Check deployment logs
tail -f /var/log/tomcat9/catalina.out
```

### 2. Application testing
```bash
# Test currency API
curl http://localhost:8080/currency-converter/api/currencies

# Test conversion
curl "http://localhost:8080/currency-converter/api/convert?amount=100&from=USD&to=EUR"
```

### 3. Web interface access
Open a browser and go to:
```
http://localhost:8080/currency-converter
```

## 🔒 Security Configuration

### 1. SSL/HTTPS Configuration (Production)

#### Generate self-signed certificate (development)
```bash
# Create keystore
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks -keysize 2048
```

#### Configure Tomcat for HTTPS
```xml
<!-- In server.xml -->
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
           maxThreads="150" scheme="https" secure="true"
           keystoreFile="conf/keystore.jks" keystorePass="password"
           clientAuth="false" sslProtocol="TLS" />
```

### 2. Firewall configuration
```bash
# Ubuntu/Debian - UFW
sudo ufw allow 8080
sudo ufw allow 8443  # For HTTPS

# CentOS/RHEL - firewalld
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp
sudo firewall-cmd --reload
```

## 📊 Monitoring and Logs

### 1. Log configuration
Logs are configured in `src/main/resources/logback.xml` and written to:
- Console (development)
- File `logs/currency-converter.log`

### 2. Log rotation
```bash
# Configure logrotate (Linux)
sudo nano /etc/logrotate.d/currency-converter
```
```
/path/to/logs/currency-converter.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
}
```

## 🚨 Troubleshooting

### Common problems and solutions

#### Port 8080 already in use
```bash
# Find process using the port
sudo lsof -i :8080

# Kill the process or change port in server.xml
```

#### Java memory error
```bash
# Increase JVM memory
export CATALINA_OPTS="-Xmx1024m -Xms512m"
```

#### Permission issues
```bash
# Give proper permissions to Tomcat
sudo chown -R tomcat:tomcat /var/lib/tomcat9/webapps/
sudo chmod -R 755 /var/lib/tomcat9/webapps/
```

#### ExchangeRate API unavailable
The application uses a free API that may have limitations:
- 1000 requests/month limit for free plan
- If limit reached, consider upgrading to a paid plan
- Alternative: change API in `ExchangeRateService.java`

## 🔄 Application Update

### 1. Stop the application
```bash
sudo systemctl stop tomcat9
```

### 2. Deploy new version
```bash
# Remove old version
sudo rm -rf /var/lib/tomcat9/webapps/currency-converter*

# Deploy new version
sudo cp target/currency-converter.war /var/lib/tomcat9/webapps/
```

### 3. Restart
```bash
sudo systemctl start tomcat9
```

## 📞 Support

In case of problems:
1. Check logs in `logs/currency-converter.log`
2. Check Tomcat logs in `/var/log/tomcat9/`
3. Test API endpoints with `curl`
4. Check Internet connectivity for external API

---

**Installation successful? The application is now ready to convert currencies! 🎉**