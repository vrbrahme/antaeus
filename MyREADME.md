This ReadMe aims to elaborate a bit on the design principle and the decisions made for this test. The solutions adds
to the existing code base by keeping the assumptions for the code intact so as to not over-engineer the solution. The
assumptions are as follows:

1. The use cases in question are fairly simple. The idea of the solution is to exhibit how can we process invoices
on a specific date using the given data model. Thus we are not really engineering for huge scale but for simplicity.
2. The application will be constantly running on a server. If that is not the case, a scheduler is not required and
a check for the date can be done every time the application is being started.
3. The volumes of invoices to process will be in the range of a few thousand per day and therefore can be processed in
the same day. Higher volumes will obviously need to ensure that this job needs to be run on a daily basis and not only
on a specific day of the month.
5. Simple use cases also mean that any changes to the code are minimal and do not largely affect the base design. If
large enhancements are to be expected then an undestanding of their nature is expected.
6. Updates to invoices can be only done on the status of the invoice and everything else is considered to be a new
invoice. So any discrepancy in the invoice requires deletion of the said invoice and creation of a new one with the
correct data.
7. Smaller volumes and no other way to update existing records also means that transactions can be limited towards the
end of the operation when executing the database query. Because the job is the only code that updates existing records,
there is no need to plan for concurrent updates.

If any of these assumptions are not true, then we need to redesign the solution accordingly.
