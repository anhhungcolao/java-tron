const fs = require('fs');

const files = []

const readAllFile = (fileName) => {
  return new Promise((resolve) => {
    fs.readdir(fileName, (err, files) => {
      files.forEach(file => {
        if (fs.statSync(fileName + '/' + file).isDirectory()) {
          readAllFile(fileName + '/' + file)
        } else {
          const extension = file.split('.').pop()
          if (extension === 'java') {
            files.push(fileName + '/' + file)
          }
        }
      })
    })
    resolve()
  })
}

const PATH = '/home/tran/code/java-tron/src/main/java/org/tron'

const buildDag = () => {
  const data = fs.readFileSync('fileName.txt', 'utf-8')
  const fileName = data.split('\n')
  const adj = []
  const par = {}
  for (let i = 0; i < fileName.length; i++) {
    adj[fileName[i]] = []
    par[fileName[i]] = 0
  }
  for (let i = 0; i < fileName.length - 2; i++) {
    const fileData = fs.readFileSync(fileName[i], 'utf-8')
    const dataLine = fileData.split('\n')
    for (let j = 0; j < dataLine.length; j++) {
      if (dataLine[j].indexOf('import') !== -1) {
        const filePath = dataLine[j].slice(0, dataLine[j].length - 2).split(' ')[1].split('.')
        if (filePath[1] == 'tron') {
          let currentPath = PATH
          for (let t = 2; t < filePath.length - 1; t++) {
            currentPath = currentPath + '/' + filePath[t]
          }
          adj[fileName[i]].push(currentPath + '/' + filePath[filePath.length - 1] + '.java')
          par[currentPath + '/' + filePath[filePath.length - 1] + '.java']++
        }
      }
    }
  }
  const que = []
  for (let i = 0; i < fileName.length; i++) {
    if (par[fileName[i]] === 0) {
      que.push(fileName[i])
    }
  }

  for (let i = 0; i < que.length; i++) {
    const u = que[i]
    console.log(u)
    for (let j = 0; j < adj[u]; j++) {
      const v = adj[u][j];
      par[v]--;
      if (par[v] === 0) que.push(v)
    }
  }
}

buildDag()