async function onloadIndexBody() {
    let rooms = await fetch("/api/chess/rooms/")
        .then(handleErrors)
        .catch(function (error) {
            alert(error.message);
        })
    rooms = await rooms.json();
    let roomSpace = document.querySelector("ul.class-list");
    Object.values(rooms.roomResponseDtos).forEach(function (value) {

        //li-시작------------------------------------------------
        const li = document.createElement("li");
        li.className = "class-card";

        //li-img
        const img = document.createElement("img");
        img.className = "class-image";
        img.setAttribute("src", "images/room.jpg");
        li.appendChild(img);

        //li-div.class-container - 시작-------------------
        const div = document.createElement("div");
        div.className = "class-container";


        //li-div.class-container-div.class-skill 시작------
        const divInnerFirst = document.createElement("div");
        divInnerFirst.className = "class-skill";

        const divInnerFirstId = document.createElement("div");
        divInnerFirstId.className = "class-type";
        divInnerFirstId.innerText = value.id + "번방";

        const divInnerFirstUpdate = document.createElement("div");
        divInnerFirstUpdate.className = "class-update";
        divInnerFirstUpdate.setAttribute("onclick", "updateRoomName(" + value.id + ");");
        divInnerFirstUpdate.innerText = "수정";

        const divInnerFirstDelete = document.createElement("div");
        divInnerFirstDelete.className = "class-delete";
        divInnerFirstDelete.setAttribute("onclick", "deleteRoom(" + value.id + ");");
        divInnerFirstDelete.innerText = "삭제";

        divInnerFirst.appendChild(divInnerFirstId);
        divInnerFirst.appendChild(divInnerFirstUpdate);
        divInnerFirst.appendChild(divInnerFirstDelete);
        //li-div.class-container-div.class-skill 끝------


        //li-div.class-container-div.class-container 시작------
        const divInnerSecond = document.createElement("div");
        divInnerSecond.className = "class-title-container";

        const divInnerSecondTitle = document.createElement("div");
        divInnerSecondTitle.className = "class-title";
        divInnerSecondTitle.innerText = value.name;

        const divInnerSecondJoin = document.createElement("div");
        divInnerSecondJoin.className = "class-join";
        divInnerSecondJoin.setAttribute("onclick", "enterRoom(" + value.id + ");");
        divInnerSecondJoin.innerText = "입 장";

        divInnerSecond.appendChild(divInnerSecondTitle);
        divInnerSecond.appendChild(divInnerSecondJoin);
        //li-div.class-container-div.class-container 끝------

        //li-div.class-container - 끝---------------------
        div.appendChild(divInnerFirst);
        div.appendChild(divInnerSecond);
        //li-끝 ------------------------------------------------
        li.appendChild(div);

        roomSpace.appendChild(li);
    });
}

async function createRoom() {
    const roomName = window.prompt("방 제목을 중복되지 않도록 입력해주세요.");

    const bodyValue = {
        name: roomName
    }
    let response = await fetch("/api/chess/rooms/", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json;charset=utf-8',
            'Accept': 'application/json'
        },
        body: JSON.stringify(bodyValue)
    })
        .then(handleErrors)
        .catch(function (error) {
            alert(error.message);
        });

    window.location.reload();
}

async function handleErrors(response) {
    if (!response.ok) {
        let message = await response.json();
        throw Error(message.errorMessage);
    }
    return response;
}

async function enterRoom(id) {
    let response = await fetch("/api/chess/rooms/" + id)
        .then(handleErrors)
        .catch(function (error) {
            alert(error.message);
        })

    window.location.replace("/game.html?id=" + id);
}


function updateRoomName(id) {
    const roomName = window.prompt("바꿀 방 제목을 입력해주세요");

    let f = document.createElement("form");
    f.setAttribute("method", "post");
    f.setAttribute("action", "/room/update/"); //url
    document.body.appendChild(f);

    let i = document.createElement("input");
    i.setAttribute("type", "hidden");
    i.setAttribute("name", "roomName"); // key
    i.setAttribute("value", roomName); // value
    f.appendChild(i);

    let i2 = document.createElement("input");
    i2.setAttribute("type", "hidden");
    i2.setAttribute("name", "roomId"); // key
    i2.setAttribute("value", id); // value
    f.appendChild(i2);

    f.submit();
}


function deleteRoom(id) {

    let f = document.createElement("form");
    f.setAttribute("method", "post");
    f.setAttribute("action", "/room/delete/"); //url
    document.body.appendChild(f);

    let i = document.createElement("input");
    i.setAttribute("type", "hidden");
    i.setAttribute("name", "roomId"); // key
    i.setAttribute("value", id); // value
    f.appendChild(i);

    console.log(f);
    f.submit();
}
