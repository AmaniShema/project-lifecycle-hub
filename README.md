# 🚀 Project Life-Cycle Hub

> A professional offline desktop application for tracking personal and professional projects through structured life-cycle stages.

Built with **Java 21 + JavaFX + SQLite** — no server, no internet, no accounts required.

![Version](https://img.shields.io/badge/version-2.2.0-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Windows%20%7C%20macOS-lightgrey)
![License](https://img.shields.io/badge/license-MIT-green)

---

## ✨ Features

| Feature | Description |
|---|---|
| **6 Life-Cycle Stages** | IDEA → PLANNING → BUILDING → TESTING → LAUNCH → MAINTENANCE |
| **Task Management** | Create, complete, delete tasks per stage |
| **Task Priorities** | HIGH / MEDIUM / LOW with colour-coded badges |
| **Due Dates** | Set deadlines with OVERDUE and DUE SOON alerts |
| **Task Notes** | Collapsible notes panel per task |
| **Phase Overview** | Stage-level notes for goals and context |
| **PDF Export** | 8-page professional report with full hierarchy |
| **Archive Projects** | Hide completed projects without deleting data |
| **Edit Projects** | Rename and update description at any time |
| **Dark / Light Theme** | Toggle with one click, remembered on restart |
| **Fully Offline** | No internet required — all data stored locally |

---

## 📸 Screenshots

> *Add screenshots of your app here after upload*

---

## 🖥️ Requirements

| Platform | Requirement |
|---|---|
| **Linux** | Java 21 + Maven 3.6+ |
| **Windows** | Java 21 (JDK) + Maven 3.6+ |
| **macOS** | Java 21 (JDK) + Maven 3.6+ |

---

## ⚡ Quick Start — Run From Source (All Platforms)

This works on **Linux, Windows, and macOS** as long as you have Java 21 and Maven installed.

### 1. Install Java 21

**Linux (Ubuntu/Debian):**
```bash
sudo apt install openjdk-21-jdk -y
java -version
```

**Windows:**
- Download from: https://adoptium.net/temurin/releases/?version=21
- Run the `.msi` installer
- Verify: open Command Prompt and run `java -version`

**macOS:**
```bash
brew install openjdk@21
java -version
```
Or download from: https://adoptium.net/temurin/releases/?version=21

---

### 2. Install Maven

**Linux (Ubuntu/Debian):**
```bash
sudo apt install maven -y
mvn -version
```

**Windows:**
- Download from: https://maven.apache.org/download.cgi
- Extract the ZIP to `C:\Program Files\Maven`
- Add `C:\Program Files\Maven\bin` to your system PATH
- Verify: open Command Prompt and run `mvn -version`

**macOS:**
```bash
brew install maven
mvn -version
```

---

### 3. Clone and Run

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/project-lifecycle-hub.git

# Navigate to the project
cd project-lifecycle-hub

# Run the application
mvn javafx:run
```

The app will launch. The database (`lifecyclehub.db`) is created automatically in your home directory on first run.

---

## 📦 Build a Native Executable

You can package the app as a native executable for your operating system using `jpackage` (built into JDK 21).

### Linux
```bash
mvn package -DskipTests
mkdir -p jpackage-input
cp target/project-lifecycle-hub-1.0.0.jar jpackage-input/

jpackage \
  --type app-image \
  --input jpackage-input \
  --main-jar project-lifecycle-hub-1.0.0.jar \
  --main-class com.lifecyclehub.ui.Launcher \
  --name ProjectLifeCycleHub \
  --app-version 2.2.0 \
  --dest ~/ProjectLifeCycleHub-App
```
Launch: `~/ProjectLifeCycleHub-App/ProjectLifeCycleHub/bin/ProjectLifeCycleHub`

### Windows
```cmd
mvn package -DskipTests
mkdir jpackage-input
copy target\project-lifecycle-hub-1.0.0.jar jpackage-input\

jpackage ^
  --type app-image ^
  --input jpackage-input ^
  --main-jar project-lifecycle-hub-1.0.0.jar ^
  --main-class com.lifecyclehub.ui.Launcher ^
  --name ProjectLifeCycleHub ^
  --app-version 2.2.0 ^
  --dest %USERPROFILE%\ProjectLifeCycleHub-App
```
Launch: Double-click `ProjectLifeCycleHub-App\ProjectLifeCycleHub\ProjectLifeCycleHub.exe`

### macOS
```bash
mvn package -DskipTests
mkdir -p jpackage-input
cp target/project-lifecycle-hub-1.0.0.jar jpackage-input/

jpackage \
  --type app-image \
  --input jpackage-input \
  --main-jar project-lifecycle-hub-1.0.0.jar \
  --main-class com.lifecyclehub.ui.Launcher \
  --name ProjectLifeCycleHub \
  --app-version 2.2.0 \
  --dest ~/ProjectLifeCycleHub-App
```
Launch: `open ~/ProjectLifeCycleHub-App/ProjectLifeCycleHub.app`

---

## 🗂️ Project Structure
project-lifecycle-hub/
├── src/main/java/com/lifecyclehub/
│   ├── controller/     # UI controllers (dashboard, project detail)
│   ├── database/       # SQLite connection + schema migrations
│   ├── entity/         # Data models (Project, Stage, Task)
│   ├── repository/     # SQL queries
│   ├── service/        # Business logic + PDF export
│   ├── ui/             # JavaFX entry point + window management
│   └── util/           # Theme manager
├── src/main/resources/
│   ├── css/            # obsidian.css (dark) + light-theme.css
│   ├── fxml/           # dashboard.fxml + project_detail.fxml
│   └── icon.png        # App icon
└── pom.xml             # Maven build configuration
---

## 🗄️ Database

The SQLite database is created automatically at:
- **Linux/macOS:** `~/lifecyclehub.db`
- **Windows:** `C:\Users\YourName\lifecyclehub.db`

No setup required. Safe migrations run on every startup — upgrading from any previous version will not lose data.

---

## 📄 PDF Export

Exported reports are saved to:
- **Linux/macOS:** `~/Documents/ProjectName_Report_YYYY-MM-DD.pdf`
- **Windows:** `C:\Users\YourName\Documents\ProjectName_Report_YYYY-MM-DD.pdf`

Each report contains:
- Cover page with project metadata
- Project summary with stage breakdown table
- One page per stage with Phase Overview, task list, task notes, and progress bar

---

## 🏗️ Architecture

The application uses **Clean Layered Architecture**:
UI Layer          → FXML + Controllers
Service Layer     → Business logic
Repository Layer  → SQL queries only
Entity Layer      → Data classes
Database Layer    → Connection + migrations
Each layer only communicates with the layer directly below it.

---

## 📋 Tech Stack

- **Java 21** — OpenJDK LTS
- **JavaFX 21.0.2** — Desktop UI framework
- **SQLite 3** via sqlite-jdbc 3.45.1.0
- **iText 5.5.13.3** — PDF generation
- **Apache Maven 3.6+** — Build tool
- **jpackage** — Native executable packaging

---

## 👤 Author

**Amani Shema**
Software Engineering Student — AUCA, Kigali, Rwanda

---

## 📜 License

MIT License — free to use, modify, and distribute.
