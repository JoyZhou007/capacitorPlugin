import { WebPlugin } from "@capacitor/core";
import { AwesomePluginPlugin } from "./definitions";

export class AwesomePluginWeb extends WebPlugin implements AwesomePluginPlugin {
  constructor() {
    super({
      name: "AwesomePlugin",
      platforms: ["web"],
    });
  }

  async testEvent(): Promise<void> {
    console.log("listen test event success...");
  }

  async storeContact(options: {
    yourName: string;
    address: Object;
    isAwesome: boolean;
  }): Promise<void> {
    console.log("storContact", options);
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log("ECHO", options);
    return options;
  }
}

const AwesomePlugin = new AwesomePluginWeb();

export { AwesomePlugin };

import { registerWebPlugin } from "@capacitor/core";
registerWebPlugin(AwesomePlugin);
