# 🚀 Guide d'Installation - Convertisseur de Devises

Ce guide vous accompagne étape par étape pour installer et déployer l'application de convertisseur de devises.

## ⚙️ Prérequis Système

### Logiciels requis
- **Java Development Kit (JDK) 21 ou supérieur**
- **Apache Maven 3.6 ou supérieur**
- **Serveur d'applications Java** (recommandé: Apache Tomcat 9+)

### Vérification des prérequis
```bash
# Vérifier Java
java -version
# Sortie attendue: java version "21.x.x" ou supérieur

# Vérifier Maven
mvn -version
# Sortie attendue: Apache Maven 3.6.x ou supérieur
```

## 📦 Installation des Prérequis

### Sur Ubuntu/Debian
```bash
# Mise à jour du système
sudo apt update

# Installation de Java 21
sudo apt install openjdk-21-jdk

# Installation de Maven
sudo apt install maven

# Installation de Tomcat
sudo apt install tomcat9 tomcat9-admin
```

### Sur CentOS/RHEL/Fedora
```bash
# Installation de Java 21
sudo dnf install java-21-openjdk-devel

# Installation de Maven
sudo dnf install maven

# Installation de Tomcat
sudo dnf install tomcat tomcat-webapps tomcat-admin-webapps
```

### Sur macOS (avec Homebrew)
```bash
# Installation de Java 21
brew install openjdk@21

# Installation de Maven
brew install maven

# Installation de Tomcat
brew install tomcat
```

### Sur Windows
1. **Java**: Télécharger depuis [Oracle](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://adoptium.net/)
2. **Maven**: Télécharger depuis [Apache Maven](https://maven.apache.org/download.cgi)
3. **Tomcat**: Télécharger depuis [Apache Tomcat](https://tomcat.apache.org/download-90.cgi)

## 🏗️ Construction du Projet

### 1. Préparation du projet
```bash
# Se placer dans le répertoire du projet
cd currency-converter

# Nettoyer et compiler
mvn clean compile
```

### 2. Exécution des tests (optionnel)
```bash
# Lancer les tests unitaires
mvn test
```

### 3. Packaging de l'application
```bash
# Créer le fichier WAR
mvn package

# Le fichier WAR sera créé dans target/currency-converter.war
ls -la target/currency-converter.war
```

## 🚢 Déploiement

### Option 1: Déploiement Rapide avec Maven (Développement)

```bash
# Démarrage avec Tomcat intégré
mvn tomcat7:run

# L'application sera accessible à:
# http://localhost:8080/currency-converter
```

### Option 2: Déploiement sur Tomcat Standalone

#### A. Configuration de Tomcat
```bash
# Définir les variables d'environnement
export CATALINA_HOME=/opt/tomcat  # Ajuster selon votre installation
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk  # Ajuster selon votre installation
```

#### B. Déploiement du WAR
```bash
# Copier le fichier WAR
sudo cp target/currency-converter.war $CATALINA_HOME/webapps/

# Ou pour une installation système (Ubuntu/Debian)
sudo cp target/currency-converter.war /var/lib/tomcat9/webapps/
```

#### C. Configuration des utilisateurs Tomcat (optionnel)
```bash
# Éditer le fichier tomcat-users.xml
sudo nano $CATALINA_HOME/conf/tomcat-users.xml

# Ajouter un utilisateur admin:
```
```xml
<tomcat-users>
  <role rolename="manager-gui"/>
  <role rolename="admin-gui"/>
  <user username="admin" password="admin123" roles="manager-gui,admin-gui"/>
</tomcat-users>
```

#### D. Démarrage de Tomcat
```bash
# Démarrer Tomcat
sudo $CATALINA_HOME/bin/startup.sh

# Ou pour une installation système
sudo systemctl start tomcat9
sudo systemctl enable tomcat9  # Démarrage automatique
```

### Option 3: Déploiement Docker (Avancé)

#### Créer un Dockerfile
```dockerfile
FROM tomcat:9-jdk21-openjdk
COPY target/currency-converter.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

#### Construire et lancer
```bash
# Construire l'image Docker
docker build -t currency-converter .

# Lancer le conteneur
docker run -p 8080:8080 currency-converter
```

## 🔧 Configuration Post-Déploiement

### 1. Vérification du déploiement
```bash
# Vérifier que Tomcat est en cours d'exécution
sudo systemctl status tomcat9

# Vérifier les logs de déploiement
tail -f /var/log/tomcat9/catalina.out
```

### 2. Test de l'application
```bash
# Tester l'API des devises
curl http://localhost:8080/currency-converter/api/currencies

# Tester une conversion
curl "http://localhost:8080/currency-converter/api/convert?amount=100&from=USD&to=EUR"
```

### 3. Accès à l'interface web
Ouvrir un navigateur et aller à :
```
http://localhost:8080/currency-converter
```

## 🔒 Configuration de Sécurité

### 1. Configuration SSL/HTTPS (Production)

#### Générer un certificat auto-signé (développement)
```bash
# Créer un keystore
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks -keysize 2048
```

#### Configurer Tomcat pour HTTPS
```xml
<!-- Dans server.xml -->
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
           maxThreads="150" scheme="https" secure="true"
           keystoreFile="conf/keystore.jks" keystorePass="password"
           clientAuth="false" sslProtocol="TLS" />
```

### 2. Configuration du pare-feu
```bash
# Ubuntu/Debian - UFW
sudo ufw allow 8080
sudo ufw allow 8443  # Pour HTTPS

# CentOS/RHEL - firewalld
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8443/tcp
sudo firewall-cmd --reload
```

## 📊 Monitoring et Logs

### 1. Configuration des logs
Les logs sont configurés dans `src/main/resources/logback.xml` et sont écrits dans :
- Console (développement)
- Fichier `logs/currency-converter.log`

### 2. Rotation des logs
```bash
# Configuration logrotate (Linux)
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

## 🚨 Dépannage

### Problèmes courants et solutions

#### Port 8080 déjà utilisé
```bash
# Trouver le processus utilisant le port
sudo lsof -i :8080

# Tuer le processus ou changer le port dans server.xml
```

#### Erreur de mémoire Java
```bash
# Augmenter la mémoire JVM
export CATALINA_OPTS="-Xmx1024m -Xms512m"
```

#### Problèmes de permissions
```bash
# Donner les bonnes permissions à Tomcat
sudo chown -R tomcat:tomcat /var/lib/tomcat9/webapps/
sudo chmod -R 755 /var/lib/tomcat9/webapps/
```

#### API ExchangeRate indisponible
L'application utilise une API gratuite qui peut avoir des limitations :
- Limite de 1000 requêtes/mois pour le plan gratuit
- En cas de limite atteinte, envisager de s'inscrire pour un plan payant
- Alternative : changer l'API dans `ExchangeRateService.java`

## 🔄 Mise à jour de l'application

### 1. Arrêter l'application
```bash
sudo systemctl stop tomcat9
```

### 2. Déployer la nouvelle version
```bash
# Supprimer l'ancienne version
sudo rm -rf /var/lib/tomcat9/webapps/currency-converter*

# Déployer la nouvelle version
sudo cp target/currency-converter.war /var/lib/tomcat9/webapps/
```

### 3. Redémarrer
```bash
sudo systemctl start tomcat9
```

## 📞 Support

En cas de problème :
1. Consulter les logs dans `logs/currency-converter.log`
2. Vérifier les logs Tomcat dans `/var/log/tomcat9/`
3. Tester les endpoints API avec `curl`
4. Vérifier la connectivité Internet pour l'API externe

---

**Installation réussie ? L'application est maintenant prête à convertir des devises ! 🎉**