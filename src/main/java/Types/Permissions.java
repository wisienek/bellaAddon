package Types;

public enum Permissions {

  // utility
  ADMIN("beloris.admin"), TEST("beloris.test"), // money
  PORTFEL_ADMIN("beloris.portfel.admin"), BANK_ADMIN("beloris.bank.admin"), // enchanting
  ENCHANTER("bella.enchanter"), CHECK_ENCHANT("bella.enchanter.check"), // teleporting
  ATP_ADMIN("bella.atp.admin"), OTP_USE("bella.otp.use"), OTP_ENCHANT("bella.otp.enchant"),
  OTP_WS("bella.otp.ws");

  private final String text;

  Permissions(
      final String text
  ) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
