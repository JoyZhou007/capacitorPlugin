declare module "@capacitor/core" {
    interface PluginRegistry {
        AwesomePlugin: AwesomePluginPlugin;
    }
}
export interface AwesomePluginPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    testEvent(): Promise<void>;
    storeContact(options: {
        yourName: string;
        address: Object;
        isAwesome: boolean;
    }): Promise<void>;
}
