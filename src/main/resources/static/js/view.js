var shownStatus = {};

function writePage(data){
  $("#main").html("");
  $("#main").append("<h1 class='main_daimei'>" + data.name + "</h1>");
  $("#main").append("<ul id='main-questions'></ul>");
  let id = "#main-questions"
  for(let key of sortedKeys(data.questions)){
    var questionData = data.questions[key];
    writeQuestion(questionData, id);
  }
}

function writeQuestion(data, id){
  $(id).append("<div id='questions-" + data.id + "'></div>");
  var childId = "#questions-" + data.id;
  if(shownStatus[data.id] == null){
    $(childId).append("<li><span class='question1' id='question-span-" + data.id + "' onclick='changeShownStatus(\"" + data.id + "\")'>" + data.name + "</span><ul id='question-ul-" + data.id + "' style='display:none'></ul><div id='question-div-" + data.id + "'></div></li>");
    shownStatus[data.id] = false;
  } else if(shownStatus[data.id]){
    $(childId).append("<li><span class='question1' id='question-span-" + data.id + "' onclick='changeShownStatus(\"" + data.id + "\")'>" + data.name + "</span><ul id='question-ul-" + data.id + "' style='display:block'></ul><div id='question-div-" + data.id + "'></div></li>");
  } else{
    $(childId).append("<li><span class='question1' id='question-span-" + data.id + "' onclick='changeShownStatus(\"" + data.id + "\")'>" + data.name + "</span><ul id='question-ul-" + data.id + "' style='display:none'></ul><div id='question-div-" + data.id + "'></div></li>");
  }
  if(Object.keys(data.questions).length == 0){
    childId = "#question-div-" + data.id;
    $(childId).html("<div class='understand'>理解度メーター </div><h1 style='display:inline;'>😱</h1><input class='meta' type='range' id='question-input-" + data.id + "' onchange='sendData(\"" + data.id + "\")'><h1 style='display:inline;'>😊</h1><font id='not-input-" + data.id + "' color='#d00'></font>");
    if(data.difficulty == -1){
      $("#not-input-" + data.id).html("<b>未入力<b>");
    } else{
      $("#question-input-" + data.id).val(100-data.difficulty);
    }
  }else{
    childId = "#question-ul-" + data.id;
    for(let key of sortedKeys(data.questions)){
      var questionData = data.questions[key];
      writeQuestion(questionData, childId);
    }
  }
}

function changeShownStatus(id){
  if(shownStatus[id]){
    shownStatus[id] = false;
    $("#question-ul-" + id).css("display", "none");
  } else{
    shownStatus[id] = true;
    $("#question-ul-" + id).css("display", "block");
  }
}

function sortedKeys(questions) {
  var keys = Object.keys(questions);
  for (var i = 0; i < keys.length - 1; i++){
    for (let j = 0; j < keys.length - i - 1; j++) {
      let key = keys[j];
      let keyPlus = keys[j+1];
      if( questions[key].name > questions[keyPlus].name ){
        let keep = keys[j];
        keys[j] = keys[j+1];
        keys[j+1] = keep;
      }
    }
  }
  return keys;
}