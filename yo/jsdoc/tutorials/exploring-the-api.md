
One of the best ways to explore the api is to do so via the browser's console.

If you are a mac user type cmd+shift+k every one else type f12.

The API has been exposed globally via the 'tc' variable.

So for instance if you wanted to select all users who's name begins with 'J' you could type:

	tc.icat('LILS').query("select user from User user where user.fullName like "J%").log()

The log method does the equivalent of:

	tc.icat('LILS').query("select user from User user where user.fullName like "J%").then(function(results){
		console.log(results);
	});

it is just a convenience method to make your life easier when debugging or experimenting.

The rest of 'tc' variable's documentation can be found [here](tc.html).