// pages/contact.js
import React from 'react';
import Layout from '../components/Layout';
import { Mail, Phone, MessageSquare, Clock } from 'lucide-react';

export default function Contact() {
  const contactMethods = [
    {
      icon: Mail,
      title: "Email Support",
      description: "Get in touch via email",
      contact: "support@hit11.com",
      hint: "24/7 response"
    },
    {
      icon: Phone,
      title: "Phone Support",
      description: "Call our support team",
      contact: "+91 1800-XXX-XXXX",
      hint: "Mon-Sat, 9AM-6PM"
    },
    {
      icon: MessageSquare,
      title: "In-App Support",
      description: "Chat with us in the app",
      contact: "Open HIT11 App",
      hint: "Instant responses"
    }
  ];

  return (
    <Layout>
      {/* Hero Section */}
      <div className="bg-blue-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h1 className="text-4xl font-bold mb-6">Contact Us</h1>
          <p className="text-xl max-w-2xl mx-auto">
            We're here to help! Reach out to us through any of our support channels.
          </p>
        </div>
      </div>

      {/* Contact Methods */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid md:grid-cols-3 gap-8">
            {contactMethods.map((method, index) => {
              const Icon = method.icon;
              return (
                <div key={index} className="bg-white p-8 rounded-lg shadow-lg text-center">
                  <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-6">
                    <Icon className="w-8 h-8 text-blue-600" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">{method.title}</h3>
                  <p className="text-gray-600 mb-4">{method.description}</p>
                  <div className="font-semibold text-blue-600 mb-2">{method.contact}</div>
                  <div className="text-sm text-gray-500">{method.hint}</div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Contact Form */}
      <div className="bg-gray-50 py-16">
        <div className="max-w-3xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Send Us a Message</h2>
          <form className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Name
                </label>
                <input
                  type="text"
                  className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Your name"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email
                </label>
                <input
                  type="email"
                  className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="your@email.com"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Subject
              </label>
              <input
                type="text"
                className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="How can we help?"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Message
              </label>
              <textarea
                rows="6"
                className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="Type your message here..."
              ></textarea>
            </div>
            <button
              type="submit"
              className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700"
            >
              Send Message
            </button>
          </form>
        </div>
      </div>

      {/* Support Hours */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="flex items-center justify-center mb-6">
              <Clock className="w-8 h-8 text-blue-600 mr-3" />
              <h2 className="text-2xl font-bold">Support Hours</h2>
            </div>
            <div className="grid md:grid-cols-2 gap-8">
              <div>
                <h3 className="font-semibold mb-4">Email & Chat Support</h3>
                <p className="text-gray-600">24/7 Available</p>
              </div>
              <div>
                <h3 className="font-semibold mb-4">Phone Support</h3>
                <p className="text-gray-600">Monday to Saturday</p>
                <p className="text-gray-600">9:00 AM to 6:00 PM IST</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* App Download CTA */}
      <div className="bg-blue-600 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-6">Get Instant Support in the App</h2>
          <p className="text-xl mb-8">Download HIT11 for 24/7 in-app support</p>
          <button className="bg-white text-blue-600 px-8 py-4 rounded-lg font-semibold hover:bg-blue-50">
            Download Now
          </button>
        </div>
      </div>
    </Layout>
  );
}