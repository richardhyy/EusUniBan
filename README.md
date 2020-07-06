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
| /uniban lookup \<**Name**>                                   |
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
  BroadcastActiveModeEnabled: 'UniBan broadcast service is running under active mode.'
  BroadcastFailed: 'Failed starting broadcast server'
  UpToDate: 'You are up-to-date.'
  NewVersionAvailable: 'There is a newer version %s available at §n https://www.spigotmc.org/resources/74747/'
  InvalidSpigotResourceID: 'It looks like you are using an unsupported version of UniBan. Please manually look for update.'
  FailedCheckingUpdate: 'Error occurred when checking update'
  LoadedFromLocalCache: 'Loaded %s banned players from ban-list cache.'
  Processing: 'Just a sec...'
  HelpMessageHeader: 'Usage:'
  HelpMessageList:
    - '/uniban lookup <&lUUID&r>'
    - '/uniban check <&lPlayer/UUID&r>'
    - '/uniban whitelist <“&ladd&r”/“&lremove&r”> <&lPlayer/UUID>'
    - '/uniban share <&lYour Server Hostname&r, eg. &nexample.com&r>'
    - '/uniban subscribe <&lSubscription Key&r>'
    - '/uniban exempt <&lServer Address&r>'
    - '/uniban reload'
```
