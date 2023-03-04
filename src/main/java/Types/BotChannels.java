package Types;

public enum BotChannels {

  ChatLogId("960200738468925480"),
  MoneyLogId("885517500261998633"),
  VariousLogId("969288905813803008"),
  HelpopLogId("1002690952307163256");

  private final String text;

  BotChannels (
      final String text
  ) {
    this.text = text;
  }

  @Override
  public String toString () {
    return text;
  }
}
