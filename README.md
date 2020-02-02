# EusUniBan

A decentralized ban-list sharing plugin for Minecraft server.

![ScreenShot.png](https://raw.githubusercontent.com/leavessoft/EusUniBan/master/ScreenShot.png)
![Description_EN.png](https://raw.githubusercontent.com/leavessoft/EusUniBan/master/Description_EN.png)

## Features

* Sharing your ban-list without a central server
* Benefiting from other servers without changing your own local ban-list
* Subscribing servers you trust
* Encrypting your shared ban-list with customizable password
* Access control:
  * Server whitelist
  * Server blacklist
  * Request frequency controlling



## Extra Requirement

* An open TCP port that is accessible by public



## Commands

| Command                                          |
| ------------------------------------------------ |
| /uniban check <Player/UUID>                      |
| /uniban whitelist <"add"/"remove"> <Player/UUID> |
| /uniban reload                                   |



## Permissions

| Permission         | Description                                                  | Default |
| ------------------ | ------------------------------------------------------------ | ------- |
| uniban.admin       | Permission to use /uniban command                            | ops     |
| uniban.getnotified | Permission to get notified when a player who reached the warning threshold enters | ops     |
| uniban.ignore      | Permission to bypass warning and banning                     | ops     |



## Subscription

*config.yml -> Subscription*

```yaml
Subscription:
  0: #tag of the server, must be unique, can be customized
    Host: 'example.com' #Host name or IP
    Port: 60009 #Port of UniBan Broadcast
    Password: 'UniBan' #You may ask for the password from the server owner
```



## Configuration

```yaml
ConfigVersion: 2
Settings:
  # Warn players with permission "uniban.getnotified" if they are banned by more than the value below, set to -1 to disable
  WarnThreshold: 1
  # Prevent players without permission "uniban.ignore" entering the server if they are banned by more than the value below, set to -1 to disable
  BanThreshold: 2
  Broadcast:
    Enabled: false
    # 0.0.0.0 if you want other servers access your ban-list
    Host: 0.0.0.0
    Port: 60009
    Threads: 2
    # If you do not want to encrypt your shared ban-list, set it to ''
    Password: UniBan
    AccessControl:
      # Simple function to protect your broadcast service from CC attack
      Enabled: true
      # Unit: minute
      MinPeriodPerServer: 1.0
      Blacklist:
        Enabled: false
        IPList:
        # If there is a '*' in this list while whitelist is enabled, only these servers that are in the whitelist can access your ban-list
        - '*'
      Whitelist:
        Enabled: true
        IPList:
        # The following servers will not be limited by "MinPeriodPerServer" function
        - localhost
    # ServerID function is still under construction
    ServerID: 7aa71b85-8f12-4472-a34f-3f0863901035
  Tasks:
    # Interval of refreshing local ban-list (players that you banned by using /ban command), the unit is minute
    LocalBanListRefreshPeriod: 1.0
    # Interval of refreshing subscribed ban-list, the unit is minute
    SubscriptionRefreshPeriod: 10.0
Subscription:
  '0':
    Host: example.com
    Port: 60009
    Password: UniBan
# Player whitelist, defined by UUID
UUIDWhitelist: []
Message:
  # '&' will automatically be replace by '§'
  WarningMessage: '&bUniban &3&l> &ePlayer {player}{uuid} has been banned from another {number} server(s).'
  BannedOnlineKickMessage: '&eSorry, you have been banned from another {number} server(s).'
```



## Coming Soon

* The ability for operators to handle emergency situations about broadcast server
* Working with third-party ban managing plugins
* Fully Bungeecord Supported
* Server Identifier
* Reusing the port of Minecraft server
* Ban-sharing status display which can be accessed in browsers



## Change Log

### 1.0 Snapshot3

* Add:
  * Threshold settings for warning and preventing when a player banned online entering
  * Update checker
* Fix:
  * Error when config was deleted before reloading
  * SubscriptionRefreshPeriod will not be loaded when broadcast is disabled
  * Tab complete still work even if a player does not have permission "uniban.admin"
  * The player would not be removed even if he/she is unbanned from all subscribed servers

### 1.0 Snapshot2

* Partly support for Bungeecord
* Fixed:
  * Hostname duplication in ban-list cache
  * Wrong hostname displayed in ban-list cache
