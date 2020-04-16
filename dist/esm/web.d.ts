import { WebPlugin } from "@capacitor/core";
import { AwesomePluginPlugin } from "./definitions";
export declare class AwesomePluginWeb extends WebPlugin implements AwesomePluginPlugin {
    constructor();
    testEvent(): Promise<void>;
    storeContact(options: {
        yourName: string;
        address: Object;
        isAwesome: boolean;
    }): Promise<void>;
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
declare const AwesomePlugin: AwesomePluginWeb;
export { AwesomePlugin };
