function main()
{
	var userName = args.u;
    var person = people.getPerson(userName);
    var groups = people.getContainerGroups(person);
    model.grouplist = groups;
}

main();
