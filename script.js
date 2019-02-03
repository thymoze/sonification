// When the user scrolls the page, execute myFunction 
window.onscroll = function () { stickyHeader() };

// Get the header
var header = document.getElementById("header");
var topButton = document.getElementById("topButton");

// Get the offset position of the navbar
var sticky = header.offsetTop;

// Add the sticky class to the header when you reach its scroll position. Remove "sticky" when you leave the scroll position
function stickyHeader() {
    if (window.pageYOffset > sticky) {
        header.classList.add("sticky");
        topButton.style.display = "block";
    } else {
        header.classList.remove("sticky");
        topButton.style.display = "none";
    }
}

// When the user clicks on the button, scroll to the top of the document
function topFunction() {
    document.body.scrollTop = 0; // For Safari
    document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
  }