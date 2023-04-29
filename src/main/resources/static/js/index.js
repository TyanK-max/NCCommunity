$(function(){
	$("#publishBtn").click(publish);
	$("#recipient-name").blur(
		function () {
			var title = $("#recipient-name").val();
			if(title == null){
				$("#title-feedback").css({display:block});
				return false;
			}
		}
	);
});

function publish() {
	$("#publishModal").modal("hide");
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	var image = document.getElementById('i-file').files[0];
	$.ajax({
		url:"https://upload-cn-east-2.qiniup.com",
		method: "post",
		processData: false,
		contentType: false,
		data:new FormData($("#uploadForm")[0]),
		success: function (data) {
			if(data && data.code === 0){
				$.post(
					CONTEXT_PATH + "/discuss/add",
					{"title":title,"content":content},
					function (data) {
						data = $.parseJSON(data);
						$("#hintBody").text(data.msg);
						$("#hintModal").modal("show")
						setTimeout(function(){
							$("#hintModal").modal("hide");
							if(data.code === 0){
								window.location.reload();
							}else {
								alert("发布帖子失败");
							}
						}, 2000);
					}
				);
				$.post(
					CONTEXT_PATH + "/files/upload",
					{'UUID':$("input[name='key']").val(),'fileName':image.name,'fileSize':image.size,'fileType':image.type},
					function (data) {
						data = $.parseJSON(data);
						if(data.code === 0){
							alert("上传成功！");
						}else{
							alert("上传失败！");
						}
					}
				)
			}else{
				alert("上传文件失败");
			}
		}
	})
	
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e,xhr,options) {
	// 	xhr.setRequestHeader(header,token);
	// })

}