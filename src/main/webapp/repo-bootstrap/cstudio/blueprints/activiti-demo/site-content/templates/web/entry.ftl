<#import "/templates/system/common/cstudio-support.ftl" as studio />

<html lang="en">
	<head>
  		<meta charset="UTF-8">
 		<title>Crafter Activiti Form Example</title>
 		<script src="//code.jquery.com/jquery-3.1.0.min.js" integrity="sha256-cCueBR6CsyA4/9szpPfrX3s49M9vUU5BgtiJj06wt/s=" crossorigin="anonymous"></script>

		<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/dt-1.10.12/datatables.min.css"/>
		<script type="text/javascript" src="https://cdn.datatables.net/v/dt/dt-1.10.12/datatables.min.js"></script>
	</head>

	<body>
		<h1>Pick a Process and Start It</h1>
		<select id="procDefs"></select>

		<button id='startProcessBtn'>Start Process</button>

		<h1>Select a Task</h1>
		<table id="tasks" class="display" cellspacing="0" width="100%">
	        <thead>
	            <tr> <th>ID</th> <th>Task Name</th> <th>Assignee</th> <th>Created</th> </tr>
	        </thead>
	    </table>
	    <div id="form"></div>

		<script>
			var crafterActiviti = {
				urls: {
					GET_PROC_DEFS:  "/api/1/services/activiti/1/get-process-defs.json",
					GET_START_PROC: "/api/1/services/activiti/1/start-process.json",
					GET_TASKS:      "/api/1/services/activiti/1/get-tasks.json",
					GET_TASK_FORM:  "/api/1/services/activiti/1/get-form-def.json",
					POST_TASK_FORM: "/api/1/services/activiti/1/submit-form.json"
				}
			}
			function initProcDefList() {
				$.get(crafterActiviti.urls.GET_PROC_DEFS, function( data ) {
					var dropdownEl = document.getElementById('procDefs');

					for(var i=0; i<data.data.length; i++) {
						var proc = data.data[i];
						var option = document.createElement("option");
						option.text = proc.name;
						option.value = proc.id
						dropdownEl.add(option);
					}
				});
			}

			function initTaskList() {
				var table = $('#tasks').DataTable({ 
					ajax: {
					url: crafterActiviti.urls.GET_TASKS, 
					dataSrc: function ( json ) {
						var dataArray = [];
						for ( var i=0, ien=json.data.length ; i<ien ; i++ ) {
							var entry = json.data[i];
							dataArray[i] = [entry.id, entry.name, (entry.assignee.firstName + " " + entry.assignee.lastName), entry.created]
						}
						return dataArray;
    				}}
				});

				$('#tasks tbody').on( 'click', 'tr', function () {
        			if ( $(this).hasClass('selected') ) {
            		$(this).removeClass('selected');
        		}
        		else {
            		table.$('tr.selected').removeClass('selected');
           		 	$(this).addClass('selected');
           		 	renderForm(table.row( this ).data()[0]);
        		} });

        		setInterval( function () { table.ajax.reload(); }, 1000 );
			};

			function updateTaskList() {
			}


			var formFields = [];
			function renderForm(taskId) {
				var formEl = $("#form");
				formEl.html("<h1>Complete Task</h1>");
				formFields = [];


				var formContent = document.createElement("div");

				$.get(crafterActiviti.urls.GET_TASK_FORM + "?taskId="+taskId, function( data ) {
					var fields = data.fields[0].fields[1];
					var len = fields.length;

					for ( var i=0 ; i<len ; i++ ) {
						var field = fields[i];

						if(field.type == "text") {
							var fieldContainerEl = document.createElement("div");
							var labelEl = document.createElement("label");
							labelEl.innerHTML = field.name;
							labelEl.for = field.id;
							labelEl.id = field.id+"Label";
							var inputEl = document.createElement("input");
							inputEl.id = field.id;
							inputEl.value = field.value;
							formEl.append(fieldContainerEl);
							fieldContainerEl.appendChild(labelEl);
							fieldContainerEl.appendChild(inputEl);
							formFields[formFields.length] = field.id;

						}
						else if(field.type = "multi-line-text") {
							var fieldContainerEl = document.createElement("div");
							var labelEl = document.createElement("label");
							labelEl.innerHTML = field.name;
							labelEl.for = field.id;
							labelEl.id = field.id+"Label";
							var inputEl = document.createElement("textarea");
							inputEl.id = field.id;
							inputEl.value = field.value;
							formEl.append(fieldContainerEl);
							fieldContainerEl.appendChild(labelEl);
							fieldContainerEl.appendChild(inputEl);
							formFields[formFields.length] = field.id;	 						
						}
					}

					var fieldContainerEl = document.createElement("div");
					var inputEl = document.createElement("button");
					inputEl.id = "formSubmit";
					inputEl.taskId = taskId;
					inputEl.innerHTML = "Complete";
					formEl.append(fieldContainerEl);
					fieldContainerEl.appendChild(inputEl);
					$("#formSubmit").taskId = taskId;	

					$("#formSubmit").click(function() {
					    var taskId = document.getElementById("formSubmit").taskId;
					    var formData = {taskId: taskId, data: {  } }

						var len = formFields.length;

						for ( var i=0 ; i<len ; i++ ) {
							var elId = formFields[i]
							var el = document.getElementById(elId);
							var value = el.value;

							formData.data[elId] = value;
						}

						$.ajax({
						    type: "POST",
						    url: crafterActiviti.urls.POST_TASK_FORM,
						    data: JSON.stringify(formData),
						    contentType: "application/json; charset=utf-8",
						    dataType: "json",
						    success: function(data) {
								updateTaskList();
  								formEl.html("");
						    },
						    failure: function(errMsg) {
						        alert(errMsg);
						    }
						});
  							
  					 
					});
  				});
			};


			$( "#startProcessBtn" ).click(function() {
				var dropdownEl = document.getElementById('procDefs');

  				$.get(crafterActiviti.urls.GET_START_PROC + "?processDef="+dropdownEl.value, function( data ) {
  					alert("process started");
  					updateTaskList();
  				});
			});

			$(document).ready(function() {
				initProcDefList();
				initTaskList();    
			});
		</script>

	</body>
</html>

