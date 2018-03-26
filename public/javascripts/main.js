if (window.console) {
  console.log("Welcome to your Play application's JavaScript!");
}

function setWindowHeight(){
    var windowHeight = window.innerHeight;
    document.body.style.height = windowHeight + "px";
    console.log(document.body.style.height);
}
