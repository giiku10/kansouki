function sendData(id) {
  $("#not-input-" + id).text("");
  let value = 100 - Number($("#question-input-" + id).val());
  $.post('/sendValue', {partId: id, value: value}, function(data){
    console.log(data);
  });
}