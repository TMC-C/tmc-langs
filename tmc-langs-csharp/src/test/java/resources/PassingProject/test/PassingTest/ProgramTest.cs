using System;
using Xunit;

namespace PassingTest 
{
    public class ProgramTest: IDisposable
    {
        [Fact]
        public void Test1()
        {
            Assert.Equal("a", "a");
        }
    }
}
