import React from 'react';
import { Download, Smartphone, Shield, HelpCircle } from 'lucide-react';

export default function DownloadPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="bg-blue-600 text-white py-20">
        <div className="max-w-7xl mx-auto px-4">
          <div className="text-center">
            <h1 className="text-4xl font-bold mb-6">Download HIT11 App</h1>
            <p className="text-xl mb-8">Join millions of cricket fans in making predictions!</p>
            <div className="flex justify-center space-x-4">
              <button className="bg-white text-blue-600 px-8 py-4 rounded-lg font-semibold hover:bg-gray-100 flex items-center">
                <Download className="w-6 h-6 mr-2" />
                Download APK
              </button>
              <button className="border-2 border-white px-8 py-4 rounded-lg font-semibold hover:bg-white hover:text-blue-600 flex items-center">
                <Smartphone className="w-6 h-6 mr-2" />
                Scan QR Code
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Installation Guide */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">How to Install</h2>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-white p-6 rounded-lg shadow-lg">
              <div className="text-center mb-4">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
                  <Download className="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center mb-4">1. Download APK</h3>
              <p className="text-gray-600">Click the download button above to get the latest version of HIT11</p>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-lg">
              <div className="text-center mb-4">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
                  <Shield className="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center mb-4">2. Allow Installation</h3>
              <p className="text-gray-600">Enable "Install from Unknown Sources" in your device settings</p>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-lg">
              <div className="text-center mb-4">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
                  <Smartphone className="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <h3 className="text-xl font-semibold text-center mb-4">3. Install & Play</h3>
              <p className="text-gray-600">Open the APK and follow installation prompts to start playing</p>
            </div>
          </div>
        </div>
      </div>

      {/* System Requirements */}
      <div className="bg-white py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">System Requirements</h2>
          <div className="max-w-2xl mx-auto bg-gray-50 rounded-lg p-6">
            <div className="space-y-4">
              <div className="flex items-start">
                <div className="w-40 font-semibold">Android Version</div>
                <div>Android 6.0 and above</div>
              </div>
              <div className="flex items-start">
                <div className="w-40 font-semibold">Storage Space</div>
                <div>Minimum 50MB free space</div>
              </div>
              <div className="flex items-start">
                <div className="w-40 font-semibold">RAM</div>
                <div>2GB or higher recommended</div>
              </div>
              <div className="flex items-start">
                <div className="w-40 font-semibold">Internet</div>
                <div>Active internet connection required</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* FAQ Section */}
      <div className="py-16">
        <div className="max-w-7xl mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">Common Questions</h2>
          <div className="max-w-3xl mx-auto space-y-6">
            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-start">
                <HelpCircle className="w-6 h-6 text-blue-600 mr-4 flex-shrink-0 mt-1" />
                <div>
                  <h3 className="font-semibold mb-2">Is it safe to install APK?</h3>
                  <p className="text-gray-600">Yes, our APK is digitally signed and verified. We regularly update it for security.</p>
                </div>
              </div>
            </div>
            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-start">
                <HelpCircle className="w-6 h-6 text-blue-600 mr-4 flex-shrink-0 mt-1" />
                <div>
                  <h3 className="font-semibold mb-2">Having installation issues?</h3>
                  <p className="text-gray-600">Contact our support team for immediate assistance with installation.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Support Banner */}
      <div className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-2xl font-bold mb-4">Need Help?</h2>
          <p className="mb-6">Our support team is available 24/7 to assist you</p>
          <button className="bg-white text-gray-900 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100">
            Contact Support
          </button>
        </div>
      </div>
    </div>
  );
}