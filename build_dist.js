const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const BASE_DIR = path.resolve(__dirname);
const DIST_DIR = path.join(BASE_DIR, "dist");

if (!fs.existsSync(DIST_DIR)) {
  fs.mkdirSync(DIST_DIR, { recursive: true });
}

const folders = fs.readdirSync(BASE_DIR).filter(f => f.startsWith("nexoria-"));

console.log(`Found ${folders.length} Nexoria plugins. Packaging...`);

folders.forEach((pluginFolder, i) => {
  const pluginDir = path.join(BASE_DIR, pluginFolder);
  const javaDir = path.join(pluginDir, "src/main/java");
  const resDir = path.join(pluginDir, "src/main/resources");
  const binDir = path.join(pluginDir, "bin");

  if (!fs.existsSync(binDir)) {
    fs.mkdirSync(binDir, { recursive: true });
  }

  // Find all .java files
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
  
  try {
    // Compile
    const javaFilesStr = javaFiles.map(f => `"${f}"`).join(" ");
    execSync(`javac -d "${binDir}" --release 21 ${javaFilesStr}`, { stdio: 'pipe' });

    // Copy resources
    if (fs.existsSync(resDir)) {
      const resFiles = fs.readdirSync(resDir);
      resFiles.forEach(r => {
        fs.copyFileSync(path.join(resDir, r), path.join(binDir, r));
      });
    }

    // Jar packaging
    const jarName = `${pluginFolder.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('')}-1.0.0.jar`;
    const jarOutput = path.join(DIST_DIR, jarName);
    
    execSync(`jar --create --file "${jarOutput}" -C "${binDir}" .`, { stdio: 'pipe' });
    console.log(`[${i + 1}/${folders.length}] Packaged: ${jarName}`);
  } catch (err) {
    console.error(`Error building ${pluginFolder}:`, err.message);
  }
});

console.log(`\nAll plugins compiled & packaged into /dist/!`);
