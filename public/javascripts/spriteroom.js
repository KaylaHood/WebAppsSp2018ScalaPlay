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

//Create WebSocket connection.
var socket = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/socket');

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

$(document).ready(function() {
	$("body").height($("window").height());
	// Set up canvas and draw new client's sprite
	var canvas = $("#sprite-canvas");
	canvas.attr('height', ($("body").height() - $("#sprite-header").height()));
	canvas.attr('width', ($("body").width()));
	
	mySprite.xPos = (canvas.width()) / 2;
	mySprite.yPos = (canvas.height()) / 2;
	spriteRadius = (canvas.width() / 100);
	if(spriteRadius <= 0) {
		spriteRadius = 1;
	}
	
	$(window).keydown(function(event) {
		if(event.which == leftArrowKeyCode) {
			mySprite.xPos -= 1;
			mySprite.xPos = (mySprite.xPos < 0) ? 0 : mySprite.xPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.which == rightArrowKeyCode) {
			mySprite.xPos += 1;
			mySprite.xPos = (mySprite.xPos > canvas.width()) ? canvas.width() : mySprite.xPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.which == upArrowKeyCode) {
			mySprite.yPos -= 1;
			mySprite.yPos = (mySprite.yPos < 0) ? 0 : mySprite.yPos;
			socket.send(JSON.stringify(mySprite));
		} else if(event.which == downArrowKeyCode) {
			mySprite.yPos += 1;
			mySprite.yPos = (mySprite.yPos > canvas.height()) ? canvas.height() : mySprite.yPos;
			socket.send(JSON.stringify(mySprite));
		}
	});
	$(window).keyup(function(event) {
		if(event.which == leftArrowKeyCode) {
			console.log("left arrow released");
		} else if(event.which == rightArrowKeyCode) {
			console.log("right arrow released");
		} else if(event.which == upArrowKeyCode) {
			console.log("up arrow released");
		} else if(event.which == downArrowKeyCode) {
			console.log("down arrow released");
		}
	});
	
	setInterval(render,100);
	
});

function clearCanvas(ctx) {
	ctx.fillStyle = "#2F2F2F";
	ctx.fillRect(0,0, $("#sprite-canvas").width(), $("#sprite-canvas").height());
}

function drawClientSprite(ctx) {
	ctx.fillStyle = "#00FF00";
	ctx.strokeStyle = "#FFFFFF";
	ctx.lineWidth = 2;
	ctx.beginPath();
	ctx.arc(mySprite.xPos, mySprite.yPos, mySprite.radius, 0, 2 * Math.PI);
	ctx.closePath();
	ctx.fill();
	ctx.stroke();
}

function render() {
	let canvas = $("#sprite-canvas")[0];
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		ctx.clearRect(0, 0, $("#sprite-canvas").width(), $("#sprite-canvas").height());
		clearCanvas(ctx);
		drawClientSprite(ctx);
	}
}

function clamp(val, min, max) {
	let res = (val > max ? max : val < min ? min : val);
    return res;
}