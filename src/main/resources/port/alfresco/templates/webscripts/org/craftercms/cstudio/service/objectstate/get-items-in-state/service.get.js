var site = args.site;
var state = args.state;

var states = new java.util.ArrayList();

if(state != "ALL") {
   states.add(state);
}
else {
	states.add("NEW_UNPUBLISHED_LOCKED");
	states.add("NEW_UNPUBLISHED_UNLOCKED"); 
    states.add("NEW_SUBMITTED_WITH_WF_SCHEDULED");
    states.add("NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_WITH_WF_UNSCHEDULED");
    states.add("NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_NO_WF_SCHEDULED");
    states.add("NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_NO_WF_UNSCHEDULED");
    states.add("NEW_PUBLISHING_FAILED");
    states.add("NEW_DELETED");
    states.add("EXISTING_UNEDITED_LOCKED");
    states.add("EXISTING_UNEDITED_UNLOCKED");
    states.add("EXISTING_EDITED_LOCKED");
    states.add("EXISTING_EDITED_UNLOCKED");
    states.add("EXISTING_SUBMITTED_WITH_WF_SCHEDULED");
    states.add("EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED");
    states.add("EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_NO_WF_SCHEDULED");
    states.add("EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_NO_WF_UNSCHEDULED");
    states.add("EXISTING_PUBLISHING_FAILED");
    states.add("EXISTING_DELETED");
}

model.items = objectStateService.getObjectStateByStates(site, states);
