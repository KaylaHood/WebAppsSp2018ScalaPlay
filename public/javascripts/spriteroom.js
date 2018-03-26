var leftArrowKeyCode = 37;
var rightArrowKeyCode = 39;
var downArrowKeyCode = 40;
var upArrowKeyCode = 38;

var spriteRadius = 10;

var mySprite = {
		id: 0, // temporary until socket tells client what its id is
		xPos: 0, // temporary until window dimensions are determined
		yPos: 0 //  temporary until window dimensions are determined
};

var spritesInRoom = {};

var canvas;

var socket;
var socketTimer;

document.addEventListener('DOMContentLoaded', (function() {
	setWindowHeight();
	// Set up canvas and draw new client's sprite
	canvas = document.getElementById("sprite-canvas");
	var canvasHeight = (document.body.clientHeight - document.getElementById("sprite-header").clientHeight);
	var canvasWidth = (document.body.clientWidth);
	canvas.setAttribute('height', canvasHeight.toString() + "px");
	canvas.setAttribute('width', canvasWidth.toString() + "px");
	
	//mySprite.xPos = canvasWidth / 2;
	//mySprite.yPos = canvasHeight / 2;
	mySprite.xPos = 10;
	mySprite.yPos = 10;
	spriteRadius = (canvasWidth / 100);
	if(spriteRadius <= 0) {
		spriteRadius = 1;
	}
	
	//Create WebSocket connection.
	socket = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/socket');

	// Connection opened 
	socket.addEventListener('open', function (event) {
		socket.send(JSON.stringify(mySprite));
	});
	
	//Listen for messages
	socket.addEventListener('message', function (event) {
	    const newClientMsg = JSON.parse(event.data);
	    const newClientIsSelf = newClientMsg.isSelf;
	    const newClient = newClientMsg.clientSprite;
	    console.log(newClientMsg);
	    if((typeof newClient.id !== undefined) && (typeof newClient.xPos !== undefined) && (typeof newClient.yPos !== undefined)) {
	    	if(newClientIsSelf) {
	    		mySprite.id = newClient.id;
	    		// position data may be invalid for the "self" object sent by the websocket
	    	}
	    	else {
	    		newClientIdStr = (newClient.id).toString();
	    		spritesInRoom[newClientIdStr]['xPos'] = newClient.xPos;
	    		spritesInRoom[newClientIdStr]['yPos'] = newClient.yPos;
	    	}
	    } else {
	    	console.log("Invalid socket message");
	    }
	});
	keepAlive();
	
	document.body.addEventListener('keydown', (function(event) {
		if(event.keyCode == leftArrowKeyCode) {
			mySprite.xPos -= 1;
			mySprite.xPos = (mySprite.xPos < 0) ? 0 : mySprite.xPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.keyCode == rightArrowKeyCode) {
			mySprite.xPos += 1;
			mySprite.xPos = (mySprite.xPos > canvas.clientWidth) ? canvas.clientWidth : mySprite.xPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.keyCode == upArrowKeyCode) {
			mySprite.yPos -= 1;
			mySprite.yPos = (mySprite.yPos < 0) ? 0 : mySprite.yPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.keyCode == downArrowKeyCode) {
			mySprite.yPos += 1;
			mySprite.yPos = (mySprite.yPos > canvas.clientHeight) ? canvas.clientHeight : mySprite.yPos;
			socket.send(JSON.stringify(mySprite));
		}
	}));

	document.body.addEventListener('keyup', (function(event) {
		if(event.keyCode == leftArrowKeyCode) {
			console.log("left arrow released");
		} else if(event.keyCode == rightArrowKeyCode) {
			console.log("right arrow released");
		} else if(event.keyCode == upArrowKeyCode) {
			console.log("up arrow released");
		} else if(event.keyCode == downArrowKeyCode) {
			console.log("down arrow released");
		}
	}));
	
	setInterval(render,100);
	
}), false);

function keepAlive() { 
    var timeout = 20000;  
    if (socket.readyState == socket.OPEN) {  
        socket.send('');  
    }  
    socketTimer = setTimeout(keepAlive, timeout);  
}  
function cancelKeepAlive() {  
    if (socketTimer) {  
        clearTimeout(socketTimer);  
    }  
}

function clearCanvas(ctx) {
	ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight);
	ctx.fillStyle = "#2F2F2F";
	ctx.fillRect(0,0, canvas.clientWidth, canvas.clientHeight);
}

function drawSprite(ctx, sprite) {
	ctx.fillStyle = "#00FF00";
	ctx.strokeStyle = "#FFFFFF";
	ctx.lineWidth = 2;
	ctx.beginPath();
	ctx.arc(sprite.xPos, sprite.yPos, spriteRadius, 0, 2 * Math.PI);
	ctx.closePath();
	ctx.fill();
	ctx.stroke();
}

function render() {
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		clearCanvas(ctx);
		drawSprite(ctx,mySprite);
		for (var spriteId in spritesInRoom) {
			if (spritesInRoom.hasOwnProperty(key)) {
				var obj = spritesInRoom[spriteId];
			    drawSprite(ctx,obj);
			}
		}
	}
}

function clamp(val, min, max) {
	let res = (val > max ? max : val < min ? min : val);
    return res;
}