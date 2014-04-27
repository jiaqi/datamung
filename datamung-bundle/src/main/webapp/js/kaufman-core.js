/**
 * Validate form with AJAX against predefined servlet in service
 *
 * @param form
 *            Form to validate
 * @returns True if validation passes
 */
function validateForm(form) {
	if ($(form).find(":input[name='formBean']").length) {
		// Input with "formBean" exists!
	} else {
		alert("Input with name 'formBean' must exist to validate form!");
		return false;
	}
	$(form).parent().find(".error").html("");
	$(form).find(":input").removeClass("malformed");
	$(form).find(":button").attr("disabled", "disabled");

	var validatorUrl;
	if (document.webappFormValidatorUrl) {
		validatorUrl = document.webappFormValidatorUrl;
	} else {
		validatorUrl = document.webappBase + "/system/validate";
	}

	var valid = false;
	$.ajax({
		url : validatorUrl,
		type : "post",
		data : $(form).serialize(),
		dataType : "json",
		async : false
	}).done(function(data) {
		valid = validateFormWithResult(form, data)
	}).fail(function() {
		$(form).find(":button").removeAttr("disabled");
	});
	return valid;
}

function validateFormWithResult(form, data) {
	if (data.successful) {
		return true;
	}
	for ( var i = 0; i < data.fields.length; i++) {
		var field = data.fields[i];
		if (field.successful) {
			continue;
		}
		$(form).find(":input[name='" + field.fieldName + "']").addClass(
				"malformed");
		$(form).parent().find(".error").each(function() {
			if ($(this).attr("u:for") == field.fieldName) {
				$(this).html(field.failureMessages.join());
			}
		});
	}
	$(form).find(":button").removeAttr("disabled");
	return false;
}
