$(function (){
    $("#resetCodeBtn").click(sendCode);
    $("input").focus(clear_error)
})

function sendCode() {
    var email = $("#emailNum").val();
    if(email === ""){
        $("#email").addClass('is-invalid');
        return false;
    }
    $.post(
       CONTEXT_PATH + "/resetCode/" +email,
        function (data) {
            data = $.parseJSON(data);
            if(data.code === 0){
                alert(data.msg);
            }else {
                console.log(data)
                console.log(data.emailMsg)
                $("#emailMsg").text(data.emailMsg);
                $("#email").addClass('is-invalid');
                alert(data.msg);
            }
        }
    )
}

function clear_error() {
    $(this).removeClass("is-invalid");
}