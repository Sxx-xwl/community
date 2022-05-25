$(function () {
    $("#uploadFrom").submit(upload);
});

function upload() {

    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadFrom")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                //更新头像路径
                $.post(
                    "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            }else {
                alert("上传失败！");
            }
        }
    });

    return false;
};