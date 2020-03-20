# EusUniBan

A decentralized ban-list sharing plugin for Minecraft server.

![ScreenShot.png](https://raw.githubusercontent.com/leavessoft/EusUniBan/master/ScreenShot.png)
![Demo](https://raw.githubusercontent.com/EusMC/UniBan-Website/master/img/2_compressed.gif)

## Features

* Sharing your ban-list without a central server
* Benefiting from other servers without changing your own local ban-list
* Subscribing servers you trust
* It works with third-party banning plugins: including AdvancedBan, BungeeBan and LiteBans
* Customizable warning & banning threshold
* Encrypting your shared ban-list with customizable password
* Extendable
* Access control:
  * Server whitelist
  * Server blacklist
  * Request frequency controlling



## Extra Requirement

* An open TCP port that is accessible by public



## Commands

| Command                                                      |
| ------------------------------------------------------------ |
| /uniban check <**Player/UUID**>                              |
| /uniban whitelist <"**add**"/"**remove**"> <**Player/UUID**> |
| /uniban subscribe \<**Subscription Key**\>                   |
| /uniban share \<**Your Server Hostname**, eg. example.com\>  |
| /uniban exempt \<**Server Address**\>                        |
| /uniban reload                                               |



## Permissions

| Permission         | Description                                                  | Default |
| ------------------ | ------------------------------------------------------------ | ------- |
| uniban.admin       | Permission to use /uniban command                            | ops     |
| uniban.getnotified | Permission to get notified when a player who reached the warning threshold enters | ops     |
| uniban.ignore      | Permission to bypass warning and banning                     | null    |



## Subscription

*config.yml -> Subscription*

```yaml
Subscription:
  '0': # tag of the server, must be unique, can be customized
    Host: 'example.com' # Host name or IP
    Port: 60009 # Port of UniBan Broadcast
    Password: 'UniBan' # You may ask for the password from the server owner
  '1KBN':
    Host: 'www.eumc.cc/uniban'
    Port: 443 # Use SSL (or 80 if you want to use HTTP)
    #Password: '' # No password
```



## Configuration

```yaml
ConfigVersion: 3
Settings:
  # Warn players with permission "uniban.getnotified" if they are banned by more than the value below, set to -1 to disable
  WarnThreshold: 1
  # Prevent players without permission "uniban.ignore" entering the server if they are banned by more than the value below, set to -1 to disable
  BanThreshold: 2
  # Whether an OP should be ignored by online-ban check
  IgnoreOP: true
  Broadcast:
    Enabled: true
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
  WarningMessage: '&bUniban &3&l> &eWarning: Player {player}({uuid}) has been banned from another {number} server(s).'
  BannedOnlineKickMessage: '&eSorry, you have been banned from another {number} server(s).'
  MessagePrefix: 'UniBan &3> &r'
  IgnoredOP: 'Ignored OP: %s'
  PlayerNotExist: 'Player %s does not exist.'
  PlayerState: 'Player %s state: %s'
  PlayerBanned: '&cBanned from: '
  PlayerNormal: '&anormal'
  InvalidSubscriptionKey: '&eInvalid subscription key'
  SubscriptionKeyAdded: 'Successfully added %s to your subscription list.'
  YourSubscriptionKey: 'Here''s the sharing link of your server''s Subscription Key which contains your address and connection password:'
  SubscriptionKeyLink: 'https://uniban.eumc.cc/share.php?key=%s'
  SubscriptionExempted: 'Successfully exempted server %s from subscription list temporarily.'
  FailedExempting: 'Failed exempting %s. Does that subscription exist?'
  WhitelistAdded: 'Player %s has been added to whitelist'
  WhitelistRemoved: 'Player %s has been removed from whitelist'
  Reloaded: 'Reloaded.'
  Error: 'Error: %s'
  SubscriptionsHeader: 'Subscriptions [%s] -----'
  ThirdPartyPluginSupportHeader: 'Third-party Banning Plugin Support -----'
  Encrypted: 'Encrypted'
  PluginEnabled: '&lEnabled'
  PluginNotFound: '&oNot Found'
  BroadcastStarted: 'UniBan broadcast started on %s:%s (%s Threads)'
  BroadcastFailed: 'Failed starting broadcast server'
  UpToDate: 'You are up-to-date.'
  NewVersionAvailable: 'There is a newer version %s available at §n https://www.spigotmc.org/resources/74747/'
  InvalidSpigotResourceID: 'It looks like you are using an unsupported version of UniBan. Please manually look for update.'
  FailedCheckingUpdate: 'Error occurred when checking update'
  LoadedFromLocalCache: 'Loaded %s banned players from ban-list cache.'
  HelpMessageHeader: 'Usage:'
  HelpMessageList:
    - '/uniban check <&lPlayer/UUID&r>'
    - '/uniban whitelist <“&ladd&r”/“&lremove&r”> <&lPlayer/UUID>'
    - '/uniban share <&lYour Server Hostname&r, eg. &nexample.com&r>'
    - '/uniban subscribe <&lSubscription Key&r>'
    - '/uniban exempt <&lServer Address&r>'
    - '/uniban reload'
```



## Coming Soon

* The ability for operators to handle emergency situations about broadcast server
* Server Identifier
* Reusing the port of Minecraft server
* Ban-sharing status display which can be accessed in browsers



## Change Log

### 1.2.1

* Add:
  * Dynamically adjusting attempting intervals for each server, which is useful in situations where third-party servers are temporarily down

* Change:
  * Reduced connection timeout

* Fix:
  * Improved the stability when working with BungeeCord
  * LiteBans support not functioning normally
  * Console spam when UniBan failed connecting to third-party servers

### 1.2

* Add:
  * Extension support
  * Set MinPeriodPerServer to 0 to turn Access Frequency Control off
* Fix:
  * Dependency configurations are not working properly on BungeeCord

### 1.1

* Add:
  * Localization Support
  * Operators can now inspect the servers that a player was banned from 
  * Cached ban-list is now saved in an easy*to*read way
  * Sharing Subscription Key is easier than before (/uniban share \<Hostname\>, and then click on the URL)
  * Now you can toggle ignoring ban check for OPs, and get a notification in console if an OP is banned somewhere
* Fix:
  * Error when executing **/uniban check** command on BungeeCord in some cases
  * Broadcast status will not be displayed on BungeeCord
  * NoClassDefFoundError on BungeeCord
  * Broadcast disabled wrongly on BungeeCord
* Change:
  * Re-add the OP check in case a server does not use permission
  * Subscription refreshing messages will not be displayed now if the subscription list is empty
  * OP will not have “uniban.ignore” permission by default now in order to make “Ignore OP” check functioning

### 1.0

* Add:
  * A quick way to add/share subscriptions
  * Exempt a server by executing a command
* Fix:
  * Configuration will not be reloaded on Bungeecord
  * Misleading message when failed resolving ban-list caused by wrong password
  * Tab complete won't work for sub-commands
* Change: Update checker message

### 1.0 Snapshot4

* Add: Support for third-party banning plugins, including AdvancedBan, BungeeBan, LiteBans, for both Bukkit and Bungeecord.
* Fix: NullPointerException when player login
* Change: Default warning message

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
