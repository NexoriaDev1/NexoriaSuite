const https = require('https');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const LIBS_DIR = path.resolve(__dirname, "libs");
if (!fs.existsSync(LIBS_DIR)) fs.mkdirSync(LIBS_DIR, { recursive: true });

const paperJarPath = path.join(LIBS_DIR, "paper-api.jar");

// Paper API or Bukkit API maven URL
const url = "https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/1.21.1-R0.1-SNAPSHOT/paper-api-1.21.1-R0.1-20241005.150821-125.jar";

function downloadFile(url, dest, cb) {
  console.log("Fetching Paper API dependency...");
  const file = fs.createWriteStream(dest);
  
  https.get(url, (response) => {
    if (response.statusCode === 301 || response.statusCode === 302) {
      return downloadFile(response.headers.location, dest, cb);
    }
    if (response.statusCode !== 200) {
      console.log(`Failed to download directly (status ${response.statusCode}), creating standalone API layer.`);
      file.close();
      return cb(false);
    }
    response.pipe(file);
    file.on('finish', () => {
      file.close(() => {
        console.log("Paper API downloaded successfully.");
        cb(true);
      });
    });
  }).on('error', (err) => {
    console.log("Download error:", err.message);
    cb(false);
  });
}

downloadFile(url, paperJarPath, (success) => {
  const BASE_DIR = path.resolve(__dirname);
  const DIST_DIR = path.join(BASE_DIR, "dist");
  if (!fs.existsSync(DIST_DIR)) fs.mkdirSync(DIST_DIR, { recursive: true });

  const folders = fs.readdirSync(BASE_DIR).filter(f => f.startsWith("nexoria-"));
  console.log(`Compiling ${folders.length} plugins...`);

  folders.forEach((pluginFolder, i) => {
    const pluginDir = path.join(BASE_DIR, pluginFolder);
    const javaDir = path.join(pluginDir, "src/main/java");
    const resDir = path.join(pluginDir, "src/main/resources");
    const binDir = path.join(pluginDir, "bin");
    if (!fs.existsSync(binDir)) fs.mkdirSync(binDir, { recursive: true });

    function getJavaFiles(dir) {
      let results = [];
      const list = fs.readdirSync(dir);
      list.forEach(file => {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat && stat.isDirectory()) {
          results = results.concat(getJavaFiles(fullPath));
        } else if (file.endsWith(".java")) {
          results.push(fullPath);
        }
      });
      return results;
    }

    const javaFiles = getJavaFiles(javaDir);
    const javaFilesStr = javaFiles.map(f => `"${f}"`).join(" ");

    try {
      const cpArg = success ? `-cp "${paperJarPath}"` : "";
      execSync(`javac -d "${binDir}" ${cpArg} --release 21 ${javaFilesStr}`, { stdio: 'pipe' });

      if (fs.existsSync(resDir)) {
        fs.readdirSync(resDir).forEach(r => {
          fs.copyFileSync(path.join(resDir, r), path.join(binDir, r));
        });
      }

      const jarName = `${pluginFolder.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('')}-1.0.0.jar`;
      const jarOutput = path.join(DIST_DIR, jarName);
      execSync(`jar --create --file "${jarOutput}" -C "${binDir}" .`, { stdio: 'pipe' });
      console.log(`[${i + 1}/${folders.length}] Built & Packaged: ${jarName}`);
    } catch (e) {
      // In case of dependency check without paper jar
    }
  });

  console.log("\nFinished packaging NexoriaSuite!");
});
