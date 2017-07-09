# TameProtect
TameProtect is a simple plugin giving players the ability to control access to their own tameable animals. By default, all animals are protected against other players and environmental damage. Players can add other players as members of these animals, giving them the ability to mount rideable animals and also harm them.

# Features

- Animals can not be harmed by other players.
- Rideable animals can not be mounted by other players.
- Other players can be added to an animal to let them mount/harm it.
- Players can transfer or remove ownership of animals.
- Players can remove the name of owned animals.
- **All of the above can be overridden by server administrators.**
- Animals are protected from all environmental damage (configurable).
- Animals will take environmental damage when someone is riding it (configurable).
- Animals will automatically be protected when tamed (configurable).
- Tamed animals are automatically be given a name (configurable).
- New users: existing owned animals will automatically be added to TameProtect when interacted with (configurable).


# Todo

- If configured, block environmental damage if an animal is owned but does not have a protection (and one cannot be created, we need an online user for this.. or we can change that)
- Teleportation of animals. Have the player select from a list? If so, what happens with a lot of them?
- Proper database (SQL?) support and merging, disk I/O is slow.
- A method of manually adding mobs for future-proofing in case it stops being updated (All that is really needed is the animal ID and human readable name, for naming).
- Keep track of whether an animal has been renamed with a name tag after being added to the plugin - useful so we don't reset user created names on owner change/can return name tag on unname