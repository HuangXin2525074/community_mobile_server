$(function () {
    $("#codebtn").click(sendCode);
})


function sendCode() {

    var email = $("#your-email").val();

    $.post(
        CONTEXT_PATH+ "/sendCode",
        {"email":email},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
            $("#codeMsg").text(data.msg);

            }else{
            $("#codeMsg").text(data.msg);
            }


        }
    );

}