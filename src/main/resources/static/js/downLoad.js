$(function (){
    $("#fileList>li").click(getFileInfo);
    $("#downLoadBtn").click(downLoadFile);
})

function getFileInfo() {
    var idx = $(this).attr("value");
    var fileId = $("#file" + idx).val();
    $.post(
        CONTEXT_PATH + "/files/fileInfo/" + fileId,
        function (data) {
            data = $.parseJSON(data);
            console.log(data);
            if(data.code === 0){
                var file = data.fileById;
                var size = file.fileSize;
                console.log(file);
                if (size < 1024){
                    $("#fileSize").html(size + " KB")
                }else{
                    size = Math.round(size/1024);
                    $("#fileSize").html(size + " MB");
                }
                $("#fileName").html(file.fileName);
                $("#downloadCnt").html(file.downloadCnt);
                if(data.fileType === "zip") {
                    $("#fileIcon").attr("class", "bi bi-file-earmark-zip");
                }else {
                    $("#fileIcon").attr("class","bi bi-filetype-"+data.fileType); 
                }
                
                $("#ownerId").html(data.userName).attr("href","http://localhost:8080/community/user/profile/"+file.ownerId);
                $("#fileId").val(file.id);
                $("#uploadTime").html(file.uploadTime);
                $("#downLoadBtn").attr("download",file.fileName).attr("href",file.fileUrl);
            }else{
                alert("请求失败");
            }
        }
    );
}

function downLoadFile() {
    $.post(
        CONTEXT_PATH + "/files/downLoad",
        {"fileId":$("#fileId").val()},
        function (data) {
            data = $.parseJSON(data);
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show")
            setTimeout(function(){
                $("#hintModal").modal("hide");
                if(data.code === 0){
                    window.location.reload();
                }else {
                    alert("Unknown Error");
                }
            }, 2000);
        }
    )
}