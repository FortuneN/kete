using Microsoft.AspNetCore.SignalR;
using System.Collections.Concurrent;

var messages = new ConcurrentBag<string>();

var builder = WebApplication.CreateBuilder();
builder.Services.AddSignalR();
builder.Services.AddSingleton(messages);

var app = builder.Build();

app.MapHub<EventHub>("/hub");
app.MapGet("/messages", (ConcurrentBag<string> msgs) => msgs.ToArray());

app.Run();

public class EventHub : Hub
{
    private readonly ConcurrentBag<string> _messages;
    public EventHub(ConcurrentBag<string> messages) => _messages = messages;
    public void SendEvent(string data)
    {
        Console.WriteLine("EVENT-RECEIVED: " + data);
        _messages.Add(data);
    }
}
