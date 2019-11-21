'use strict';

let modalId = $('#image-gallery');
let responseList;

/**
 * @param preview
 *            This is a preview link to be added <img src.
 * 
 */
function appendResponseToGallery(preview){
	var divStr = '<div class="col-lg-3 col-md-4 col-xs-6 thumb"> <a class="thumbnail" href="#" data-image-id="" data-toggle="modal"data-title="" data-image="' + preview + '" data-target="#image-gallery"> <img class="img-thumbnail" src="' + preview +  '" alt="Another alt text"> </a> </div>';
	$('#galleryDiv').append(divStr);
}

// This function disables buttons when needed
function disableButtons(counter_max, counter_current) {
  $('#show-previous-image, #show-next-image')
    .show();
  if (counter_max === counter_current) {
    $('#show-next-image')
      .hide();
  } else if (counter_current === 1) {
    $('#show-previous-image')
      .hide();
  }
}

/**
 * 
 * @param setIDs
 *            Sets IDs when DOM is loaded. If using a PHP counter, set to false.
 * @param setClickAttr
 *            Sets the attribute for the click handler.
 */

function loadGallery(setIDs, setClickAttr) {
  let current_image,
    selector,
    counter = 0;

  $('#show-next-image, #show-previous-image')
    .click(function () {
      if ($(this)
        .attr('id') === 'show-previous-image') {
        current_image--;
      } else {
        current_image++;
      }

      selector = $('[data-image-id="' + current_image + '"]');
      updateGallery(selector);
    });

function updateGallery(selector) {
	  debugger;
    let $sel = selector;
    current_image = $sel.data('image-id');
    $('#image-gallery-title')
      .text($sel.data('title'));
    $('#image-gallery-image')
      .attr('src', $sel.data('image'));
    $('#image-gallery-infotext').text(responseList[current_image].name);
    $('#image-gallery-previewlink').text(responseList[current_image].preview);
    disableButtons(counter, $sel.data('image-id'));
  }

  if (setIDs == true) {
    $('[data-image-id]')
      .each(function () {
        counter++;
        $(this)
          .attr('data-image-id', counter);
      });
  }
  $(setClickAttr)
    .on('click', function () {
      updateGallery($(this));
    });
}

/**
 * Get all image name and preview link list from server.
 */
function uploadImageListFromServer(){
	var xhr = new XMLHttpRequest();
	
	xhr.open("GET", "/listImages");
	xhr.onload = function() {
		var response = JSON.parse(xhr.responseText);
		
		if(xhr.status == 200){
			
			responseList = response;
			
			// data null check
			if(response == null || response == undefined)
				return;
			// append valid data to gallery.
			response.forEach(element => {
				appendResponseToGallery(element.preview);
			});
			// add listeners.
			loadGallery(true, 'a.thumbnail');
		}
		if(xhr.status == 400){
			// show error message
			alert(response.message);
		}
	};
	xhr.send();
}

$(document)
  .ready(function () {
	uploadImageListFromServer();
  });

// build key actions
$(document)
  .keydown(function (e) {
    switch (e.which) {
      case 37: // left
        if ((modalId.data('bs.modal') || {})._isShown && $('#show-previous-image').is(":visible")) {
          $('#show-previous-image')
            .click();
        }
        break;

      case 39: // right
        if ((modalId.data('bs.modal') || {})._isShown && $('#show-next-image').is(":visible")) {
          $('#show-next-image')
            .click();
        }
        break;

      default:
        return; // exit this handler for other keys
    }
    e.preventDefault(); // prevent the default action (scroll / move caret)
  });
