package com.example.tournafy.di;

import com.example.tournafy.service.impl.AuthService;
import com.example.tournafy.service.impl.EventService;
import com.example.tournafy.service.impl.HostingService;
import com.example.tournafy.service.impl.TournamentService;
import com.example.tournafy.service.interfaces.IAuthService;
import com.example.tournafy.service.interfaces.IEventService;
import com.example.tournafy.service.interfaces.IHostingService;
import com.example.tournafy.service.interfaces.ITournamentService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class ServiceModule {

    // Note: Using @Provides instead of @Binds because AuthService is a Singleton
    // that might need explicit instantiation logic or has a private constructor.

    @Provides
    @Singleton
    public IAuthService provideAuthService() {
        return AuthService.getInstance();
    }

    // For standard services with @Inject constructors, we can just create them via the provider
    // or use @Binds in an abstract module. @Provides is explicit and safe here.

    @Provides
    @Singleton
    public IHostingService provideHostingService(HostingService impl) {
        return impl;
    }

    @Provides
    @Singleton
    public IEventService provideEventService(EventService impl) {
        return impl;
    }

    @Provides
    @Singleton
    public ITournamentService provideTournamentService(TournamentService impl) {
        return impl;
    }
}