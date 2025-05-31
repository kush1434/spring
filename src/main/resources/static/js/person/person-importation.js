async function importPeople(fileContent) {
    let data1;
    await fetch("/mvc/import/all/person", {  
        method: "POST",
        body: fileContent,
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("import-all").addEventListener("click", async () => {
    const input = document.getElementById("personAllFileUpload");
    if(input.files.length != 1){
        alert("You must upload a file.")
        return;
    }
    let file = input.files[0];
    let text = await file.text()
    if(!Array.isArray(JSON.parse(text))){
        alert("This import is expecting an array.")
        return;
    }
    let content = await importPeople(text);
})