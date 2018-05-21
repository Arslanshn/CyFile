package at.tugraz.tc.cyfile;

import android.app.Application;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;

import com.blankj.utilcode.util.Utils;

import at.tugraz.tc.cyfile.async.AsyncModule;
import at.tugraz.tc.cyfile.async.impl.JobExecutor;
import at.tugraz.tc.cyfile.crypto.CryptoService;
import at.tugraz.tc.cyfile.crypto.KeyVaultService;
import at.tugraz.tc.cyfile.crypto.PrefixCryptoService;
import at.tugraz.tc.cyfile.crypto.impl.KeyVaultServiceImpl;
import at.tugraz.tc.cyfile.crypto.impl.NoOpCryptoService;
import at.tugraz.tc.cyfile.injection.ApplicationComponent;
import at.tugraz.tc.cyfile.injection.DaggerApplicationComponent;
import at.tugraz.tc.cyfile.logging.AndroidLogger;
import at.tugraz.tc.cyfile.logging.CyFileLogger;
import at.tugraz.tc.cyfile.note.NoteModule;
import at.tugraz.tc.cyfile.note.NoteRepository;
import at.tugraz.tc.cyfile.note.impl.FileNoteRepository;
import at.tugraz.tc.cyfile.note.impl.SecureNoteService;
import at.tugraz.tc.cyfile.secret.SecretModule;
import at.tugraz.tc.cyfile.secret.SecretRepository;
import at.tugraz.tc.cyfile.secret.impl.HashPinPatternSecretVerifier;
import at.tugraz.tc.cyfile.secret.impl.HashSecretRepository;
import at.tugraz.tc.cyfile.secret.impl.InMemorySecretRepository;
import at.tugraz.tc.cyfile.secret.impl.OnApplicationForegroundSecretPrompter;
import at.tugraz.tc.cyfile.secret.impl.PinPatternSecret;
import at.tugraz.tc.cyfile.secret.impl.PinPatternSecretPrompter;
import at.tugraz.tc.cyfile.secret.impl.SecretManagerImpl;
import at.tugraz.tc.cyfile.secret.impl.SimplePinPatternSecretVerifier;

/**
 * Application extended class
 * Created by cobri on 3/21/2018.
 */

public class CyFileApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }

    public static CyFileApplication get(Context context) {
        return (CyFileApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {

            KeyVaultService keyVaultService = new KeyVaultServiceImpl();

            SecretRepository secretRepository = new HashSecretRepository(this, null);
            OnApplicationForegroundSecretPrompter prompter = new OnApplicationForegroundSecretPrompter(new PinPatternSecretPrompter(this), keyVaultService);
            CyFileLogger logger = new AndroidLogger();

            NoteRepository repository = new FileNoteRepository(this, null, logger);
            repository.initialize();

            //add this prompter to the lifecycle so we which states
            ProcessLifecycleOwner.get().getLifecycle().addObserver(prompter);

            SecretModule secretModule = new SecretModule(
                    new SecretManagerImpl(
                            new HashPinPatternSecretVerifier(secretRepository),
                            secretRepository),
                    prompter,
                    keyVaultService
            );

            mApplicationComponent = DaggerApplicationComponent.builder()
                    .appModule(new AppModule(this, new AndroidLogger()))
                    .noteModule(new NoteModule(
                            new SecureNoteService(
                                    repository,
                                    new NoOpCryptoService())))
                    .asyncModule(new AsyncModule(new JobExecutor()))
                    .secretModule(secretModule)
                    .build();
        }
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        this.mApplicationComponent = applicationComponent;
    }
}
