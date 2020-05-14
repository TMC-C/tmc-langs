using System;
using Xunit;

namespace FailingTest
{
    public class ProgramTest
    {
        [Fact]
        public void Test1()
        {
            Assert.Equal("abc", "ab");
        }
    }
}
