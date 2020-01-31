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
