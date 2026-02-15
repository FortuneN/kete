FROM mcr.microsoft.com/dotnet/sdk:8.0

WORKDIR /app

RUN dotnet new web --name signalr-echo --no-https && \
    cd signalr-echo && \
    rm Program.cs

COPY Program.cs /app/signalr-echo/Program.cs

RUN cd signalr-echo && \
    dotnet build -c Release -o /app/out

ENV ASPNETCORE_URLS=http://0.0.0.0:5000

EXPOSE 5000

ENTRYPOINT ["dotnet", "/app/out/signalr-echo.dll"]
