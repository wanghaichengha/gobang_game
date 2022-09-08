let gameInfo = {
    roomId: null,
    thisUserId: null,
    thatUserId: null,
    isWhite: true,
}
// 设定界面显示相关操作
function setScreenText(me) {
    let screen = document.querySelector('#screen');
    if (me) {
        screen.innerHTML = "轮到你落子了!";
    } else {
        screen.innerHTML = "轮到对方落子了!";
    }
}

// 初始化 websocket
let websocketUrl = 'ws://' + location.host + '/game';
var websocket = new WebSocket(websocketUrl);
        //需要配合实例挂载一些回调函数
    websocket.onopen = function(){
        console.log("建立连接！");
    }
    websocket.onerror = function(){
        console.log("连接异常！");
    }
       
    websocket.onclose = function(){
        console.log("连接断开！")
    }

    window.onbeforeprint = function(){
        websocket.close;
    }

    websocket.onmessage = function(e){
        console.log("[handlerGameReady]" + e.data);
        let response = JSON.parse(e.data);

        if(!response.ok){
            alert("连接失败! reason:" + response.reason);
            location.assign('/game_hall.html');
            return;
        }

        if(response.message = "gameReady"){
            gameInfo.roomId = response.roomId;
            gameInfo.thatUserId = response.thatUserId;
            gameInfo.thisUserId = response.thisUserId;
            gameInfo.isWhite = (response.whiteUser == response.thisUserId);
            //连接成功后，初始化棋盘
            initGame();
            //设置显示区域的内容
            setScreenText(gameInfo.isWhite); 
        }else if(response.message == "repeatConnection"){
            console.log("禁止多开，请用其他账号登录！");
            //location.assign('/login.html')
            location.replace('/login.html');   
        }

    }

// 初始化一局游戏
function initGame() {
    // 是我下还是对方下. 根据服务器分配的先后手情况决定
    let me = gameInfo.isWhite;
    // 游戏是否结束
    let over = false;
    let chessBoard = [];
    //初始化chessBord数组(表示棋盘的数组)
    for (let i = 0; i < 15; i++) {
        chessBoard[i] = [];
        for (let j = 0; j < 15; j++) {
            chessBoard[i][j] = 0;
        }
    }
    let chess = document.querySelector('#chess');
    let context = chess.getContext('2d');
    context.strokeStyle = "#BFBFBF";
    // 背景图片
    let logo = new Image();
    logo.src = "image/image2.jpg";
    logo.onload = function () {
        context.drawImage(logo, 0, 0, 450, 450);
        initChessBoard();
    }

    // 绘制棋盘网格
    function initChessBoard() {
        for (let i = 0; i < 15; i++) {
            context.moveTo(15 + i * 30, 15);
            context.lineTo(15 + i * 30, 430);
            context.stroke();
            context.moveTo(15, 15 + i * 30);
            context.lineTo(435, 15 + i * 30);
            context.stroke();
        }
    }

    // 绘制一个棋子, me 为 true
    function oneStep(i, j, isWhite) {
        context.beginPath();
        context.arc(15 + i * 30, 15 + j * 30, 13, 0, 2 * Math.PI);
        context.closePath();
        var gradient = context.createRadialGradient(15 + i * 30 + 2, 15 + j * 30 - 2, 13, 15 + i * 30 + 2, 15 + j * 30 - 2, 0);
        if (!isWhite) {
            gradient.addColorStop(0, "#0A0A0A");
            gradient.addColorStop(1, "#636766");
        } else {
            gradient.addColorStop(0, "#D1D1D1");
            gradient.addColorStop(1, "#F9F9F9");
        }
        context.fillStyle = gradient;
        context.fill();
    }

    chess.onclick = function (e) {
        if (over) {
            return;
        }
        if (!me) {
            return;
        }
        let x = e.offsetX;
        let y = e.offsetY;
        // 注意, 横坐标是列, 纵坐标是行
        let col = Math.floor(x / 30);
        let row = Math.floor(y / 30);
        if (chessBoard[row][col] == 0) {
            // TODO 发送坐标给服务器, 服务器要返回结果
            send(row,col);

            //oneStep(col, row, gameInfo.isWhite);
            //chessBoard[row][col] = 1;
        }
    }
    function send(row,col){
        let request = {
            message: 'putChess',
            userId: gameInfo.thisUserId,
            row: row,
            col: col,
        }
        websocket.send(JSON.stringify(request));
    }

    //在initGame方法中重写 websocket.onmessage，用来专门处理putChess亲求
    //之前的websocket.onmessage方法只是在处理游戏就绪响应，游戏就绪后就消失了
    websocket.onmessage = function(e){
        console.log("[handlerputChess]"+e.data);
        //把服务器返回的JSON格式字符串转为js对象
        let response = JSON.parse(e.data);
        if(response.message != "putChess"){
            console.log("接收数据格式错误！");
            return;
        }
        //判断这个响应是谁洛的子
        if(response.userId == gameInfo.thisUserId){
            //自己落子
            oneStep(response.col,response.row,gameInfo.isWhite);
        }else if(response.userId == gameInfo.thatUserId){
            //对方落子
            oneStep(response.col,response.row,!gameInfo.isWhite);
        }else{
            //响应错误
            console.log("响应的userId有误" + response.userId);
            return;
        }
        //给对应的位置设置为1，表示该位置已有棋子
        chessBoard[response.row][response.col] = 1;
        //交换双方轮换落子
        me = !me;
        setScreenText(me);
        //判断游戏是否已经分出胜负
        let screenDiv = document.querySelector('#screen');
        if(response.winner != 0){
            if(response.winner == gameInfo.thisUserId){
                //lert("你赢了！");
                screenDiv.innerHTML = "你赢了！"
            }else if(response.winner == gameInfo.thatUserId){
                //alert("你输了！");
                screenDiv.innerHTML = "你输了！"
            }else{
                alert("字段错误！"+response.winner);
            }
            //回到游戏大厅
            //location.assign('/game_hall.html');
            //增加一个按钮，游戏结束，点击回到大厅
            let backButton = document.createElement('button');
            backButton.innerHTML = "回到大厅";
            backButton.onclick =function(){
                location.replace('/game_hall.html');
            }
            let fatherDiv = document.querySelector('.container>div');
            fatherDiv.appendChild(backButton);
        }


    }


}
