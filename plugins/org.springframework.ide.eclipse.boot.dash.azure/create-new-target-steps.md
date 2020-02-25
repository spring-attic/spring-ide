Some notes about the steps taken to create Azure target.

- Create a eclipse plugin to host your code
- Setup a class to define your 'injections' via SimpleDIContainer
   - see org.springframework.ide.eclipse.boot.dash.azure.BootDashInjections
   - wire it into BootDash via eclipse extension point org.springframework.ide.eclipse.boot.dash.injections
- Create a class for your new RunTargetType (AzureRunTargetType).
- Create a injection of this class in your 'BootDashInjections' class.
