$(function() {
    var changeRoom = '';

    $('#chat').hide();
    $('#alert').hide();
    var hub = $.connection.chatHub;


// Here the functions to receive the changes from the server

    hub.client.sendMessage = function(username, message) {
        var el = $('<div class="message"><span></span><p></p></div>');
        $("span", el).text(username + ': ');
        $("p", el).text(message);
        $(el).addClass("talk");
        $('#messages').append(el);
    };

    hub.client.userJoined = function(username) {
        var li = document.createElement('li');
        li.textContent = username;
        $("#members").append(li);
    };

    hub.client.userList = function(users) {
        $("#members").html('');
        $(users).each(function() {
            var li = document.createElement('li');
            li.textContent = this;
            $("#members").append(li);
        });
    };

 

    hub.client.userLeftRoom = function(username) {
        var el = $('<div class="message"><span></span><p></p></div>');
        $("span", el).text(username);
        $("p", el).text("has left the room");
        $(el).addClass("join");
        $('#messages').append(el);
    };

    hub.client.userJoinedRoom = function(username) {
        var el = $('<div class="message"><span></span><p></p></div>');
        $("span", el).text(username);
        $("p", el).text("has joined the room");
        $(el).addClass("join");
        $('#messages').append(el);
    };


// Here the functions which send information to the server 

    $.connection.hub.start().done(function () {
        $('#home').click(function() {
            hub.server.logout();
            $('#chat').hide();
            $('#login').show();
        });

        $('#loginSubmit').click(function(event) {
            event.preventDefault();
            hub.server.login($('#inputUsername').val()).done(function(result) {
                if(result) {
                    $('#login').hide();
                    $('#chat').show();
                    $('#inputUsername').val('');
                    $('#alert').hide();
                } else {
                    $('#alert').show();
                }
            });
        });
        
         $('#invitado').click(function(event) {
            event.preventDefault();
            hub.server.login('invitado').done(function(result) {
                if(result) {
                    $('#login').hide();
                    $('#chat').show();
                    $('#inputUsername').val('');
                    $('#alert').hide();
                } else {
                    $('#alert').show();
                }
            });
        });

        $('#roomselect').change(function () {
            var optionSelected = $(this).find("option:selected");
            var room = optionSelected.text();
            hub.server.joinRoom(room).done(function() {
                changedRoom(room);
            });
        });

        $('#create').click(function(event) {
            event.preventDefault();
            changeRoom = $('#roomname').val();
            hub.server.createRoom($('#roomname').val()).done(function() {
                changedRoom(changeRoom);
                $('#roomname').val('');
            });
        });

        var sendMessage = function() {
            hub.server.sendMessage($('#roomselect').val(), $("#talk").val());
            var el = $('<div class="message"><span></span><p></p></div>');
            $("span", el).text('Me: ');
            $("p", el).text($("#talk").val());
            $(el).addClass('talk');
            $(el).addClass('me');
            $('#messages').append(el);
            $("#talk").val('');
        };


        var handleReturnKey = function(e) {
            if(e.charCode == 13 || e.keyCode == 13) {
                e.preventDefault();
                sendMessage();
            }
        };

        $("#talk").keypress(handleReturnKey);
    });
});