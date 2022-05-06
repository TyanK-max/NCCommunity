$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderFul);
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId,entityUserId,postId) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e,xhr,options) {
        xhr.setRequestHeader(header,token);
    })
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
            } else {
                alert(data.msg);
            }
        }
    );
}

function setTop() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e,xhr,options) {
        xhr.setRequestHeader(header,token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code === 0){
                $("#topBtn").attr("disabled","disabled");
            }else{
                alert(data.msg)
            }
        }
    )
}
function setWonderFul() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e,xhr,options) {
        xhr.setRequestHeader(header,token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code === 0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else{
                alert(data.msg)
            }
        }
    )
}
function setDelete() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e,xhr,options) {
        xhr.setRequestHeader(header,token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code === 0){
                location.href = CONTEXT_PATH + "/index";
            }else{
                alert(data.msg)
            }
        }
    )
}